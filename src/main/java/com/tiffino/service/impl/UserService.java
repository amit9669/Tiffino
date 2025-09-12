package com.tiffino.service.impl;

import com.tiffino.entity.*;
import com.tiffino.entity.request.*;
import com.tiffino.entity.response.*;
import com.tiffino.repository.*;
import com.tiffino.service.*;
import com.tiffino.entity.User;
import com.tiffino.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private DataToken dataToken;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CloudKitchenRepository cloudKitchenRepository;

    @Autowired
    private CloudKitchenMealRepository cloudKitchenMealRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private UserGiftCardRepository userGiftCardRepository;

    @Autowired
    private GiftCardsRepository giftCardsRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private PriceCalculatorService priceCalculatorService;


    public Object registerUser(UserRegistrationRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "User already exits";
        }

        if (!emailService.isDeliverableEmail(request.getEmail())) {
            return "Invalid or undeliverable email: " + request.getEmail();
        }

        User user = User.builder()
                .userName(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNo(request.getPhoneNo())
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return "User Save Successfully!!";
    }

    @Override
    public Object getAllAvailableMealsGroupedByCuisine() {
        List<CloudKitchenMeal> availableMeals = cloudKitchenMealRepository.findByAvailableTrue();

        Map<String, Map<Long, MealResponse>> groupedByCuisine = new HashMap<>();

        for (CloudKitchenMeal ckMeal : availableMeals) {
            String cuisineName = ckMeal.getMeal().getCuisine().getName();
            Long mealId = ckMeal.getMeal().getMealId();

            groupedByCuisine
                    .computeIfAbsent(cuisineName, k -> new HashMap<>())
                    .compute(mealId, (id, mealResp) -> {
                        if (mealResp == null) {
                            return MealResponse.builder()
                                    .mealId(mealId)
                                    .mealName(ckMeal.getMeal().getName())
                                    .price(ckMeal.getMeal().getPrice())
                                    .photos(ckMeal.getMeal().getPhotos())
                                    .description(ckMeal.getMeal().getDescription())
                                    .nutritionalInformation(ckMeal.getMeal().getNutritionalInformation())
                                    .kitchens(new ArrayList<>(List.of(
                                            CloudKitchenInfo.builder()
                                                    .cloudKitchenId(ckMeal.getCloudKitchen().getCloudKitchenId())
                                                    .cloudKitchenName(ckMeal.getCloudKitchen().getCity() + " - " + ckMeal.getCloudKitchen().getDivision())
                                                    .build()
                                    )))
                                    .build();
                        } else {
                            mealResp.getKitchens().add(
                                    CloudKitchenInfo.builder()
                                            .cloudKitchenId(ckMeal.getCloudKitchen().getCloudKitchenId())
                                            .cloudKitchenName(ckMeal.getCloudKitchen().getCity() + " - " + ckMeal.getCloudKitchen().getDivision())
                                            .build()
                            );
                            return mealResp;
                        }
                    });
        }

        return groupedByCuisine.entrySet().stream()
                .map(entry -> CuisineMealsResponse.builder()
                        .cuisine(entry.getKey())
                        .meals(new ArrayList<>(entry.getValue().values()))
                        .build())
                .toList();
    }


    @Transactional
    @Override
    public Object updateCurrentUser(UserUpdationRequest req) {
        User user = (User) dataToken.getCurrentUserProfile();
        user.setUserName(req.getName());
        user.setAddress(req.getAddress());
        user.setDietaryNeeds(req.getDietaryNeeds());
        user.setMealPreference(req.getMealPreference());
        user.setPhoneNo(req.getPhoneNo());
        return "Updated Successfully!!";
    }


    @Override
    @Transactional
    public Object createOrder(CreateOrderRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();

        Optional<CloudKitchen> cloudKitchenOptional = cloudKitchenRepository.findByCloudKitchenIdAndIsDeletedFalse(request.getCloudKitchenId());

        if (!cloudKitchenOptional.isPresent()) {
            return "CloudKItchen Not Found";
        }

        CloudKitchen cloudKitchen = cloudKitchenOptional.get();

        List<CloudKitchenMeal> kitchenMeals = cloudKitchenMealRepository
                .findByCloudKitchenAndAvailableTrue(cloudKitchen);

        Map<Long, CloudKitchenMeal> availableMeals = kitchenMeals.stream()
                .collect(Collectors.toMap(cm -> cm.getMeal().getMealId(), cm -> cm));

        List<CloudKitchenMeal> orderedMeals = new ArrayList<>();
        for (Long mealId : request.getMealIds()) {
            if (!availableMeals.containsKey(mealId)) {
                return "Meal ID " + mealId + " not available in this kitchen";
            }
            orderedMeals.add(availableMeals.get(mealId));
        }

        double totalCost = orderedMeals.stream()
                .mapToDouble(cm -> cm.getMeal().getPrice())
                .sum();

        Order order = Order.builder()
                .user(user)
                .cloudKitchen(cloudKitchen)
                .ckMeals(orderedMeals)
                .orderStatus(String.valueOf(DeliveryStatus.PENDING))
                .deliveryDetails(request.getDeliveryDetails())
                .totalCost(totalCost)
                .build();

        orderRepository.save(order);
        return "Order placed successfully!";
    }


    @Override
    public Object getAllOrders() {
        User user = (User) dataToken.getCurrentUserProfile();

        List<Order> orders = orderRepository.findAllByUser_UserId(user.getUserId());

        return orders.stream()
                .map(order -> {
                    String orderDate = order.getCreatedAt().toLocalDate().toString();
                    String orderTime = order.getCreatedAt().toLocalTime().toString();

                    List<String> mealNames = order.getCkMeals().stream()
                            .map(item -> item.getMeal().getName())
                            .toList();

                    return OrderResponse.builder()
                            .orderId(order.getOrderId())
                            .orderStatus(order.getOrderStatus())
                            .totalCost(order.getTotalCost())
                            .orderDate(orderDate)
                            .orderTime(orderTime)
                            .mealName(mealNames)
                            .build();
                })
                .toList();
    }


    @Override
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }


    @Override
    @Transactional
    public Object createReview(ReviewRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();

        Order order = orderRepository.findByOrderIdAndUser_UserId(request.getOrderId(), user.getUserId()).get();

        Optional<Delivery> deliveryOptional = deliveryRepository.findByOrder_OrderId(order.getOrderId());

        if (!deliveryOptional.isPresent()) {
            return "Delivery not found for this order";
        }

        Delivery delivery = deliveryOptional.get();

        if (delivery.getStatus() != DeliveryStatus.DELIVERED) {
            return "You can only review after the order has been delivered";
        }

        if (reviewRepository.existsByOrder_OrderId(order.getOrderId())) {
            return "You have already reviewed this order";
        }

        Review review = Review.builder()
                .comment(request.getComment())
                .rating(request.getRating())
                .user(user)
                .cloudKitchen(order.getCloudKitchen())
                .order(order)
                .build();

        reviewRepository.save(review);
        return "Review submitted successfully!";
    }


    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public boolean checkUserExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Object trackOrder() {
        User user = (User) dataToken.getCurrentUserProfile();

        List<Delivery> deliveries = deliveryRepository.findAllByOrder_User_UserId(user.getUserId());

        if (!deliveries.isEmpty()) {
            return deliveries.stream()
                    .map(delivery -> DeliveryTrackingResponse.builder()
                            .orderId(delivery.getOrder().getOrderId())
                            .orderStatus(delivery.getOrder().getOrderStatus())
                            .deliveryId(delivery.getDeliveryId())
                            .deliveryStatus(delivery.getStatus())
                            .deliveryPersonName(delivery.getDeliveryPerson() != null ? delivery.getDeliveryPerson().getName() : "Not Assigned")
                            .deliveryPersonPhone(delivery.getDeliveryPerson() != null ? delivery.getDeliveryPerson().getPhoneNo() : "N/A")
                            .assignedAt(delivery.getAssignedAt())
                            .pickedUpAt(delivery.getPickedUpAt())
                            .deliveredAt(delivery.getDeliveredAt())
                            .build())
                    .toList();
        }

        List<Order> pendingOrders = orderRepository.findAllByUser_UserIdAndOrderStatus(user.getUserId(), "PENDING");

        if (!pendingOrders.isEmpty()) {
            return pendingOrders.stream()
                    .map(order -> DeliveryTrackingResponse.builder()
                            .orderId(order.getOrderId())
                            .orderStatus(order.getOrderStatus())
                            .deliveryId(null)
                            .deliveryStatus(DeliveryStatus.PENDING)
                            .deliveryPersonName("Not Assigned")
                            .deliveryPersonPhone("N/A")
                            .assignedAt(null)
                            .pickedUpAt(null)
                            .deliveredAt(null)
                            .build())
                    .toList();
        }

        return "No pending deliveries found for this user";
    }

    @Override
    public Object getAllMealsByCuisineName(String cuisineName) {
        Cuisine cuisine = cuisineRepository.findByName(cuisineName);
        return cuisine.getMeals();
    }


    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRole(Role.USER);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public Object assignSubscriptionToUser(SubscriptionRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();

        if (userSubscriptionRepository.existsByUser_UserIdAndIsSubscribedTrue(user.getUserId())) {
           return "User already has active subscription!! Please wait for it to expire.";
        }

        boolean isFile = request.getDietaryFilePath() != null;
        double originalPrice = priceCalculatorService.calculatePrice(
                request.getDurationType(),
                request.getMealTimes(),
                request.getAllergies(),
                request.getCaloriesPerMeal(),
                isFile
        );

        double finalPrice = originalPrice;
        double appliedDiscountPercent = 0.0;

        if (request.getGiftCardCodeInput() != null && !request.getGiftCardCodeInput().isBlank()) {
            UserGiftCards userGiftCards = userGiftCardRepository
                    .findByGiftCardCodeAndUser_UserIdAndIsRedeemedFalse(request.getGiftCardCodeInput(), user.getUserId()).get();
            if(userGiftCards==null){
                return "Invalid or expired offer code!";
            }

            if (userGiftCards.getValidForPlan() != request.getDurationType()) {
                throw new RuntimeException("Offer code only valid for " + userGiftCards.getValidForPlan());
            }

            finalPrice = applyOffer(originalPrice, userGiftCards);
            appliedDiscountPercent = userGiftCards.getDiscountPercent();

            userGiftCards.setIsRedeemed(true);
            userGiftCards.setRedeemedAt(LocalDateTime.now());
            userGiftCardRepository.save(userGiftCards);
        }

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .durationType(request.getDurationType())
                .mealTimes(request.getMealTimes())
                .allergies(request.getAllergies())
                .startDate(LocalDateTime.now())
                .expiryDate(calculateExpiryDate(request.getDurationType()))
                .isSubscribed(true)
                .dietaryFilePath(imageUploadService.uploadImage(request.getDietaryFilePath()))
                .finalPrice(finalPrice)
                .build();

        userSubscriptionRepository.save(subscription);

        Map<String, Object> response = new HashMap<>();
        response.put("message", appliedDiscountPercent > 0 ? "Subscribed Successfully with Discount!" : "Subscribed Successfully!");
        response.put("subscription", Map.of(
                "userSubId", subscription.getUserSubId(),
                "planType", subscription.getDurationType(),
                "startDate", subscription.getStartDate(),
                "expiryDate", subscription.getExpiryDate(),
                "originalPrice", originalPrice,
                "appliedDiscountPercent", appliedDiscountPercent,
                "finalPrice", finalPrice
        ));

        return response;
    }

    private LocalDateTime calculateExpiryDate(DurationType durationType) {
        return switch (durationType) {
            case DAILY -> LocalDateTime.now().plusDays(1);
            case WEEKLY -> LocalDateTime.now().plusWeeks(1);
            case MONTHLY -> LocalDateTime.now().plusMonths(1);
            case QUARTERLY -> LocalDateTime.now().plusMonths(3);
        };
    }

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkExpiredSubscriptions() {
        List<UserSubscription> expiredSubs =
                userSubscriptionRepository.findAllByIsSubscribedTrueAndExpiryDateBefore(LocalDateTime.now());

        for (UserSubscription sub : expiredSubs) {
            sub.setIsSubscribed(false);
            userSubscriptionRepository.save(sub);

            generateOrUpdateOfferForExpiredSubscription(sub.getDurationType(), sub.getUser());
        }
    }

    public void generateOrUpdateOfferForExpiredSubscription(DurationType expiredPlanType, User user) {

        List<String> offerTypes = Arrays.asList("LOYALTY", "WELCOME_BACK", "SURPRISE");
        String selectedType = offerTypes.get(new Random().nextInt(offerTypes.size()));


        GiftCards giftCards = giftCardsRepository.findByTypeAndIsActiveTrue(selectedType)
                .orElseGet(() -> {
                    GiftCards newGiftCard = GiftCards.builder()
                            .type(selectedType)
                            .description(getOfferDescription(selectedType))
                            .isActive(true)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return giftCardsRepository.save(newGiftCard);
                });

        Optional<UserGiftCards> existingOfferOpt = userGiftCardRepository
                .findByUser_UserIdAndValidForPlanAndIsRedeemedFalse(user.getUserId(), expiredPlanType);

        double discount = calculateDiscount(selectedType, user);

        String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        UserGiftCards userGiftCards;
        if (existingOfferOpt.isPresent()) {
            userGiftCards = existingOfferOpt.get();
            userGiftCards.setGiftCards(giftCards);
            userGiftCards.setDiscountPercent(discount);
            userGiftCards.setExpiryDate(LocalDateTime.now().plusDays(30));
            userGiftCards.setGiftCardCode(code);
        } else {
            userGiftCards = UserGiftCards.builder()
                    .user(user)
                    .giftCards(giftCards)
                    .validForPlan(expiredPlanType)
                    .giftCardCode(code)
                    .discountPercent(discount)
                    .expiryDate(LocalDateTime.now().plusDays(30))
                    .isRedeemed(false)
                    .build();
        }

        userGiftCardRepository.save(userGiftCards);
    }

    private double calculateDiscount(String type, User user) {
        long subscriptionCount = userSubscriptionRepository.countByUser_UserId(user.getUserId());
        Random random = new Random();

        return switch (type) {
            case "LOYALTY" -> (subscriptionCount <= 5)
                    ? subscriptionCount * 10.0
                    : 20 + random.nextInt(21);
            case "WELCOME_BACK" -> 25.0;
            case "SURPRISE" -> 10 + random.nextInt(31);
            default -> 15.0;
        };
    }

    private String getOfferDescription(String type) {
        return switch (type) {
            case "LOYALTY" -> "Loyalty discount for your continued support!";
            case "WELCOME_BACK" -> "Welcome back! Enjoy 25% off your next plan.";
            case "SURPRISE" -> "Surprise! A random discount just for you.";
            default -> "Special discount offer.";
        };
    }

    public double applyOffer(double originalPrice, UserGiftCards userOffer) {
        double discounted = originalPrice - (originalPrice * (userOffer.getDiscountPercent() / 100));
        return Math.round(discounted * 100.0) / 100.0;
    }

}
