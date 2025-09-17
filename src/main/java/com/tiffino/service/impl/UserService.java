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
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;


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
    public Object createOrder(DeliveryDetails deliveryDetails) {
        User user = (User) dataToken.getCurrentUserProfile();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No cart found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<CloudKitchenMeal> orderedMeals = cart.getItems().stream()
                .map(CartItem::getCloudKitchenMeal)
                .collect(Collectors.toList());

        double totalCost = cart.getTotalPrice();

        Order order = Order.builder()
                .user(user)
                .cloudKitchen(cart.getCloudKitchen())
                .ckMeals(orderedMeals)
                .orderStatus(String.valueOf(DeliveryStatus.PENDING))
                .deliveryDetails(deliveryDetails)
                .totalCost(totalCost)
                .build();

        orderRepository.save(order);

        cartRepository.delete(cart);

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
        double originalPrice = priceCalculatorService.calculatePrice(request.getDurationType(), request.getMealTimes(), request.getAllergies(), request.getCaloriesPerMeal(), isFile);
        double finalPrice = originalPrice;
        double appliedDiscountPercent = 0.0;
        if (request.getGiftCardCodeInput() != null && !request.getGiftCardCodeInput().isBlank()) {
            UserGiftCards userGiftCards = userGiftCardRepository.findByGiftCardCodeAndUser_UserIdAndIsRedeemedFalse(request.getGiftCardCodeInput(), user.getUserId()).get();
            if (userGiftCards.getValidForPlan() != request.getDurationType()) {
                throw new RuntimeException("Offer code only valid for " + userGiftCards.getValidForPlan());
            }
            finalPrice = this.applyOffer(originalPrice, userGiftCards);
            appliedDiscountPercent = userGiftCards.getDiscountPercent();
            userGiftCards.setIsRedeemed(true);
            userGiftCards.setRedeemedAt(LocalDateTime.now());
            userGiftCardRepository.save(userGiftCards);
        }

        MultipartFile file = request.getDietaryFilePath();
        String uploadedImageUrl = null;

        if (file != null && !file.isEmpty()) {
            System.out.println("UserService");
            uploadedImageUrl = imageUploadService.uploadImage(file);
        }
        UserSubscription subscription = UserSubscription.builder().user(user).durationType(request.getDurationType()).mealTimes(request.getMealTimes()).allergies(request.getAllergies()).startDate(LocalDateTime.now()).expiryDate(calculateExpiryDate(request.getDurationType())).isSubscribed(true).dietaryFilePath(uploadedImageUrl).finalPrice(finalPrice).build();
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
                "finalPrice", finalPrice));
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

    @Scheduled(cron = "0 0/2 * * * ?")
    @Transactional
    public void checkExpiredSubscriptions() {
        List<UserSubscription> expiredSubs = userSubscriptionRepository.findAllByIsSubscribedTrueAndExpiryDateBefore(LocalDateTime.now());
        for (UserSubscription sub : expiredSubs) {
            sub.setIsSubscribed(false);
            userSubscriptionRepository.save(sub);
            generateOrUpdateOfferForExpiredSubscription(sub.getDurationType(), sub.getUser());
        }
    }

    public void generateOrUpdateOfferForExpiredSubscription(DurationType expiredPlanType, User user) {
        List<String> offerTypes = Arrays.asList("LOYALTY", "WELCOME_BACK", "SURPRISE");
        String selectedType = offerTypes.get(new Random().nextInt(offerTypes.size()));
        GiftCards giftCards = giftCardsRepository.findByTypeAndIsActiveTrue(selectedType).orElseGet(() -> {
            GiftCards newGiftCard = GiftCards.builder().type(selectedType).description(getOfferDescription(selectedType)).isActive(true).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
            return giftCardsRepository.save(newGiftCard);
        });
        Optional<UserGiftCards> existingOfferOpt = userGiftCardRepository.findByUser_UserIdAndValidForPlanAndIsRedeemedFalse(user.getUserId(), expiredPlanType);
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
            userGiftCards = UserGiftCards.builder().user(user).giftCards(giftCards).validForPlan(expiredPlanType).giftCardCode(code).discountPercent(discount).expiryDate(LocalDateTime.now().plusDays(30)).isRedeemed(false).build();
        }
        userGiftCardRepository.save(userGiftCards);
    }

    private double calculateDiscount(String type, User user) {
        long subscriptionCount = userSubscriptionRepository.countByUser_UserId(user.getUserId());
        Random random = new Random();
        return switch (type) {
            case "LOYALTY" -> (subscriptionCount <= 5) ? subscriptionCount * 10.0 : 20 + random.nextInt(21);
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

    @Override
    public Object getAllGiftCardsOfUser() {
        User user = (User) dataToken.getCurrentUserProfile();
        List<UserGiftCards> giftCards = userGiftCardRepository.findByUser_UserIdAndIsRedeemedFalse(user.getUserId());

        return giftCards.stream()
                .map(gc -> GiftCardResponse.builder()
                        .userGiftCardId(gc.getUserGiftCardId())
                        .giftCardCode(gc.getGiftCardCode())
                        .discountPercent(gc.getDiscountPercent().intValue() + "%")
                        .validForPlan(gc.getValidForPlan())
                        .description(gc.getGiftCards().getDescription())
                        .build()
                )
                .toList();
    }

    @Override
    @Transactional
    public Object addMealsToCart(CartRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = cloudKitchenRepository
                .findByCloudKitchenIdAndIsDeletedFalse(request.getCloudKitchenId())
                .orElseThrow(() -> new RuntimeException("CloudKitchen Not Found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUser(user);
                    c.setCloudKitchen(cloudKitchen);
                    return c;
                });

        if (cart.getCloudKitchen() != null &&
                !cart.getCloudKitchen().getCloudKitchenId().equals(request.getCloudKitchenId())) {
            return "You can only add meals from one CloudKitchen at a time";
        }

        List<Long> mealIds = request.getMeals().stream()
                .map(CartRequest.CartMealItem::getMealId)
                .toList();

        List<CloudKitchenMeal> availableMeals = cloudKitchenMealRepository
                .findByCloudKitchenAndMeal_MealIdInAndAvailableTrue(cloudKitchen, mealIds);

        Map<Long, CloudKitchenMeal> mealMap = availableMeals.stream()
                .collect(Collectors.toMap(cm -> cm.getMeal().getMealId(), cm -> cm));

        for (CartRequest.CartMealItem itemReq : request.getMeals()) {
            CloudKitchenMeal ckm = mealMap.get(itemReq.getMealId());
            if (ckm == null) {
                throw new RuntimeException("Meal ID " + itemReq.getMealId() + " not available");
            }

            boolean exists = cart.getItems().stream()
                    .anyMatch(i -> i.getCloudKitchenMeal().getId().equals(ckm.getId()));

            if (!exists) {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setCloudKitchenMeal(ckm);
                newItem.setQuantity(0);
                newItem.setPrice(ckm.getMeal().getPrice());
                cart.getItems().add(newItem);
            }
        }

        double totalUnitPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getCloudKitchenMeal().getMeal().getPrice())
                .sum();
        cart.setTotalPrice(totalUnitPrice);

        cartRepository.save(cart);
        return "Meals added to cart without quantity. Total base price: " + totalUnitPrice;
    }



    @Override
    @Transactional
    public Object removeMealFromCart(Long mealId) {
        User user = (User) dataToken.getCurrentUserProfile();
        Cart cart = cartRepository.findByUser(user).get();

        if (cart == null) {
            return "Cart Not Found";
        }

        cart.getItems().removeIf(item ->
                item.getCloudKitchenMeal().getMeal().getMealId().equals(mealId)
        );

        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return null;
        }

        cart.setTotalPrice(
                cart.getItems().stream().mapToDouble(CartItem::getPrice).sum()
        );

        cartRepository.save(cart);

        return "Remove Meal :- " + mealId;
    }

    @Override
    public Object viewCart() {
        User user = (User) dataToken.getCurrentUserProfile();

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return "Cart is empty";
        }

        List<CartResponse.CartMealInfo> mealInfos = cart.getItems().stream()
                .map(item -> new CartResponse.CartMealInfo(
                        item.getCloudKitchenMeal().getMeal().getMealId(),
                        item.getCloudKitchenMeal().getMeal().getName(),
                        item.getCloudKitchenMeal().getMeal().getPhotos(),
                        item.getCloudKitchenMeal().getMeal().getPrice(),
                        item.getQuantity(),
                        item.getCloudKitchenMeal().getMeal().getPrice() * item.getQuantity()
                ))
                .toList();

        if (mealInfos.isEmpty()) {
            return "Cart is empty";
        }

        return new CartResponse(
                cart.getId(),
                cart.getCloudKitchen().getCloudKitchenId(),
                cart.getTotalPrice(),
                mealInfos
        );
    }

    @Override
    @Transactional
    public Object updateCartQuantities(UpdateQuantityRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart Not Found"));

        Map<Long, Integer> quantityMap = request.getItems().stream()
                .collect(Collectors.toMap(UpdateQuantityRequest.ItemQuantity::getMealId,
                        UpdateQuantityRequest.ItemQuantity::getQuantity));

        for (CartItem item : cart.getItems()) {
            Integer newQty = quantityMap.get(item.getCloudKitchenMeal().getMeal().getMealId());
            if (newQty != null && newQty >= 0) {
                item.setQuantity(newQty);
                item.setPrice(item.getCloudKitchenMeal().getMeal().getPrice() * newQty);
            }
        }

        double total = cart.getItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();

        cart.setTotalPrice(total);
        cartRepository.save(cart);

        return "Cart updated with quantities. Total Price: " + total;
    }
}
