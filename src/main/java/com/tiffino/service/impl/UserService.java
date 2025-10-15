package com.tiffino.service.impl;

import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
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

import java.awt.*;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OffersRepository offersRepository;

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

    @Autowired
    private OrderItemRepository orderItemRepository;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);


    public Object registerUser(UserRegistrationRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return "User already exits";
        }

        Future<String> future = executorService.submit(() -> {
            if (!emailService.isDeliverableEmail(request.getEmail())) {
                return "Invalid or undeliverable email: " + request.getEmail();
            }
            return "Email is valid: " + request.getEmail();
        });


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
        User user = (User) this.dataToken.getCurrentUserProfile();

        List<CloudKitchenMeal> availableMeals = cloudKitchenMealRepository.findByAvailableTrue();
        Map<String, Map<Long, MealResponse>> groupedByCuisine = new HashMap<>();

        boolean hasActiveSubscription = user != null &&
                userSubscriptionRepository.existsByUser_UserIdAndIsSubscribedTrue(user.getUserId());

        LocalDate today = LocalDate.now();
        List<Offers> todayOffers = offersRepository.findByValidDate(today).stream()
                .filter(Offers::isActive)
                .toList();

        for (CloudKitchenMeal ckMeal : availableMeals) {
            if (!ckMeal.getCloudKitchen().getIsDeleted()) {
                String cuisineName = ckMeal.getMeal().getCuisine().getName();
                Long mealId = ckMeal.getMeal().getMealId();

                double originalPrice = ckMeal.getMeal().getPrice();
                double finalPrice = originalPrice;

                if (hasActiveSubscription) {
                    finalPrice = applyDiscount(originalPrice);
                } else if (!todayOffers.isEmpty()) {
                    for (Offers offer : todayOffers) {
                        finalPrice = originalPrice * (1 - offer.getDiscountPercentage() / 100.0);
                    }
                }

                double finalPrice1 = finalPrice;
                groupedByCuisine
                        .computeIfAbsent(cuisineName, k -> new HashMap<>())
                        .compute(mealId, (id, mealResp) -> {
                            if (mealResp == null) {
                                return MealResponse.builder()
                                        .mealId(mealId)
                                        .mealName(ckMeal.getMeal().getName())
                                        .originalPrice(originalPrice)
                                        .finalPrice(finalPrice1)
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
        }

        return groupedByCuisine.entrySet().stream()
                .map(entry -> CuisineMealsResponse.builder()
                        .cuisine(entry.getKey())
                        .meals(new ArrayList<>(entry.getValue().values()))
                        .build())
                .toList();
    }

    private double applyDiscount(double price) {
        double discountRate = 0.20;
        return price - (price * discountRate);
    }


    @Transactional
    @Override
    public Object updateCurrentUser(UserUpdationRequest req) {
        User user = (User) dataToken.getCurrentUserProfile();
        User user1 = userRepository.findById(user.getUserId()).get();
        user1.setUserName(req.getName());
        user1.setAddress(req.getAddress());
        user1.setDietaryNeeds(req.getDietaryNeeds());
        user1.setMealPreference(req.getMealPreference());
        userRepository.save(user1);
        return "Updated Successfully!!";
    }


    @Transactional
    @Override
    public Object createOrder(DeliveryDetails deliveryDetails) {
        User user = (User) dataToken.getCurrentUserProfile();

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("No cart found"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        UserSubscription userSubscription = userSubscriptionRepository
                .findByIsSubscribedTrueAndUser_UserId(user.getUserId());

        if (userSubscription != null && Boolean.TRUE.equals(userSubscription.getIsSubscribed())) {
            Set<String> allergies = userSubscription.getAllergies();
            String allergyList = (allergies != null && !allergies.isEmpty())
                    ? String.join(", ", allergies)
                    : "None";
            deliveryDetails.setAllergies(allergyList);
        } else {
            deliveryDetails.setAllergies(cart.getAllergies());
        }

        double totalCost = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Order order = Order.builder()
                .user(user)
                .cloudKitchen(cart.getCloudKitchen())
                .orderStatus(String.valueOf(DeliveryStatus.PENDING))
                .deliveryDetails(deliveryDetails)
                .totalCost(totalCost)
                .isAvailable(true)
                .build();

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> OrderItem.builder()
                        .order(order)
                        .cloudKitchenMeal(ci.getCloudKitchenMeal())
                        .quantity(ci.getQuantity())
                        .price(ci.getPrice())
                        .build())
                .toList();

        order.setItems(orderItems);

        Order save = orderRepository.save(order);
        cartRepository.delete(cart);

        return save.getOrderId();
    }


    @Override
    public Object getAllOrders() {
        User user = (User) dataToken.getCurrentUserProfile();

        List<Order> orders = orderRepository.findAllByUser_UserId(user.getUserId());

        return orders.stream()
                .map(order -> {
                    String orderDate = order.getCreatedAt().toLocalDate().toString();
                    String orderTime = order.getCreatedAt().toLocalTime()
                            .truncatedTo(ChronoUnit.SECONDS).toString();

                    List<OrderResponse.OrderMealsResponse> mealsResponses =
                            order.getItems().stream()
                                    .map(item -> {
                                        OrderResponse.OrderMealsResponse r =
                                                new OrderResponse().new OrderMealsResponse();
                                        r.setMealName(item.getCloudKitchenMeal().getMeal().getName());
                                        r.setMealPhotos(item.getCloudKitchenMeal().getMeal().getPhotos());
                                        r.setMealQuantity(item.getQuantity());
                                        r.setMealPrice(item.getPrice() * item.getQuantity());
                                        return r;
                                    })
                                    .toList();

                    return OrderResponse.builder()
                            .orderId(order.getOrderId())
                            .orderStatus(order.getOrderStatus())
                            .allergies(order.getDeliveryDetails().getAllergies())
                            .totalCost(order.getTotalCost())
                            .orderDate(orderDate)
                            .orderTime(orderTime)
                            .orderMealsResponses(mealsResponses)
                            .build();
                })
                .toList();
    }


    @Override
    public void deleteOrder(Long orderId) {
        User user = (User) dataToken.getCurrentUserProfile();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("You are not allowed to cancel this order");
        }

        if ("DELIVERED".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Delivered orders cannot be cancelled");
        }

        if ("ASSIGNED_TO_DELIVERY".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Assigned orders cannot be cancelled");
        }

        if ("OUT_FOR_DELIVERY".equalsIgnoreCase(order.getOrderStatus())) {
            throw new RuntimeException("Pick Up orders cannot be cancelled");
        }

        order.setOrderStatus("CANCELLED");
        order.setIsAvailable(false);
        orderRepository.save(order);
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
                .cloudKitchenReview(request.getComment())
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
    public Object trackOrder(Long orderId) {
        User user = (User) dataToken.getCurrentUserProfile();

        Optional<Delivery> deliveryOpt =
                deliveryRepository.findByOrder_OrderIdAndOrder_User_UserId(orderId, user.getUserId());

        if (deliveryOpt.isPresent()) {
            Delivery d = deliveryOpt.get();
            return DeliveryTrackingResponse.builder()
                    .orderId(d.getOrder().getOrderId())
                    .orderStatus(d.getOrder().getOrderStatus())
                    .deliveryId(d.getDeliveryId())
                    .deliveryStatus(d.getStatus())
                    .deliveryPersonName(d.getDeliveryPerson() != null
                            ? d.getDeliveryPerson().getName() : "Not Assigned")
                    .deliveryPersonPhone(d.getDeliveryPerson() != null
                            ? d.getDeliveryPerson().getPhoneNo() : "N/A")
                    .assignedAt(d.getAssignedAt())
                    .pickedUpAt(d.getPickedUpAt())
                    .deliveredAt(d.getDeliveredAt())
                    .build();
        }

        Order order = orderRepository.findByOrderIdAndUser_UserId(orderId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("Order not found for this user"));

        if ("PENDING".equalsIgnoreCase(order.getOrderStatus())) {
            return DeliveryTrackingResponse.builder()
                    .orderId(order.getOrderId())
                    .orderStatus(order.getOrderStatus())
                    .deliveryId(null)
                    .allergies(order.getDeliveryDetails().getAllergies())
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .deliveryPersonName("Not Assigned")
                    .deliveryPersonPhone("N/A")
                    .assignedAt(null)
                    .pickedUpAt(null)
                    .deliveredAt(null)
                    .build();
        }

        if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            return "Order has Cancelled!!!";
        }

        return "No delivery or pending status found for orderId: " + orderId;
    }


    @Override
    public Object searchFilterForUser(List<String> cuisineNames, List<String> cloudKitchenNames) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (cuisineNames != null) {
            cuisineNames = cuisineNames.stream()
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        if (cloudKitchenNames != null) {
            cloudKitchenNames = cloudKitchenNames.stream()
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .collect(Collectors.toList());
        }

        List<String> targetCuisines;
        if (cuisineNames == null || cuisineNames.isEmpty()) {
            targetCuisines = cuisineRepository.findAll()
                    .stream()
                    .map(Cuisine::getState)
                    .collect(Collectors.toList());
        } else {
            targetCuisines = cuisineNames;
        }

        for (String cuisineName : targetCuisines) {
            List<Map<String, Object>> mealsByCuisine =
                    (List<Map<String, Object>>) this.getAllMealsByStateName(cuisineName);

            if (mealsByCuisine != null && !mealsByCuisine.isEmpty()) {
                results.addAll(mealsByCuisine);
            }
        }

        if (cloudKitchenNames != null && !cloudKitchenNames.isEmpty()) {
            Set<String> normalizedKitchenNames = cloudKitchenNames.stream()
                    .map(name -> name.replaceAll("\\s+", "").toLowerCase())
                    .collect(Collectors.toSet());

            results = results.stream()
                    .filter(meal -> {
                        String kitchenName = String.valueOf(meal.get("cloudKitchenName"))
                                .replaceAll("\\s+", "")
                                .toLowerCase();
                        return normalizedKitchenNames.contains(kitchenName);
                    })
                    .collect(Collectors.toList());
        }

        return results;
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
        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .durationType(request.getDurationType())
                .mealTimes(request.getMealTimes())
                .allergies(request.getAllergies())
                .startDate(LocalDateTime.now())
                .expiryDate(calculateExpiryDate(request.getDurationType()))
                .isSubscribed(true)
                .dietaryFilePath(uploadedImageUrl)
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

    @Scheduled(cron = "0 0/1 * * * ?")
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

        boolean hasActiveSubscription = userSubscriptionRepository
                .existsByUser_UserIdAndIsSubscribedTrue(user.getUserId());

        LocalDate today = LocalDate.now();
        List<Offers> todayOffers = offersRepository.findByValidDate(today).stream()
                .filter(Offers::isActive)
                .toList();

        for (CartRequest.CartMealItem itemReq : request.getMeals()) {
            CloudKitchenMeal ckm = mealMap.get(itemReq.getMealId());

            if (ckm == null) {
                throw new RuntimeException("Meal ID " + itemReq.getMealId() + " not available");
            }

            double originalPrice = ckm.getMeal().getPrice();
            double finalPrice = originalPrice;

            if (hasActiveSubscription) {
                finalPrice = applyDiscount(originalPrice);
            } else if (!todayOffers.isEmpty()) {
                for (Offers offer : todayOffers) {
                    finalPrice = originalPrice * (1 - offer.getDiscountPercentage() / 100.0);
                }
            }

            boolean exists = cart.getItems().stream()
                    .anyMatch(i -> i.getCloudKitchenMeal().getId().equals(ckm.getId()));

            if (!exists) {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setCloudKitchenMeal(ckm);
                newItem.setQuantity(1);
                newItem.setPrice(finalPrice);
                cart.getItems().add(newItem);
            } else {
                for (CartItem item : cart.getItems()) {
                    if (item.getCloudKitchenMeal().getId().equals(ckm.getId())) {
                        item.setPrice(finalPrice);
                    }
                }
            }
        }

        double totalUnitPrice = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        cart.setTotalPrice(totalUnitPrice);

        cartRepository.save(cart);
        return "Meals added to cart without quantity. Total price: " + totalUnitPrice;
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
    public Object addAllergies(List<String> allergies) {
        User user = (User) dataToken.getCurrentUserProfile();

        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            return "Cart is empty";
        }
        String allergiesResult = String.join(", ", allergies);
        Integer totalAllergies = allergies.size();
        cart.setAllergies(allergiesResult);
        cart.setTotalAllergies(totalAllergies);
        cart.setTotalPrice((cart.getTotalAllergies() * 10) + cart.getTotalPrice());
        cartRepository.save(cart);
        return allergiesResult;
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
                        item.getPrice(),
                        item.getQuantity(),
                        item.getPrice() * item.getQuantity()
                ))
                .toList();

        if (mealInfos.isEmpty()) {
            return "Cart is empty";
        }

        UserSubscription userSubscription = userSubscriptionRepository
                .findByIsSubscribedTrueAndUser_UserId(user.getUserId());

        boolean isSubscribed = false;
        if (userSubscription != null) {
            isSubscribed = userSubscription.getIsSubscribed();
        }

        return new CartResponse(
                cart.getId(),
                cart.getCloudKitchen().getCloudKitchenId(),
                cart.getCloudKitchen().getCity() + "-" + cart.getCloudKitchen().getDivision(),
                isSubscribed,
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
                .collect(Collectors.toMap(
                        UpdateQuantityRequest.ItemQuantity::getMealId,
                        UpdateQuantityRequest.ItemQuantity::getQuantity
                ));

        for (CartItem item : cart.getItems()) {
            Integer newQty = quantityMap.get(item.getCloudKitchenMeal().getMeal().getMealId());
            if (newQty != null && newQty >= 0) {
                item.setQuantity(newQty);
            }
        }

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity()) // <-- FIXED HERE
                .sum();

        cart.setTotalPrice(total);
        cartRepository.save(cart);

        return "Cart updated with quantities. Total Price: " + total;
    }


    @Override
    public void viewInvoice(Long orderId, OutputStream out) {
        User user = (User) dataToken.getCurrentUserProfile();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrder_OrderId(orderId);
        if (orderItems.isEmpty()) {
            throw new IllegalArgumentException("No items found for order " + orderId);
        }

        try {
            Document doc = new Document(PageSize.A2, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, out);
            doc.open();

            // === Colors ===
            Color shidhoriOrange = new Color(255, 102, 0);
            /*Color headerBg1 = new Color(255, 178, 102);*/
            Color headerBg2 = new Color(255, 153, 51);
            Color grayBg = new Color(245, 245, 245);

            // === Add Logo ===
            Image logo = Image.getInstance("D:\\QuantumSoft\\Tiffino\\src\\assets\\images\\shidori_logo1.png");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Element.ALIGN_CENTER);
            doc.add(logo);

            // === Title ===
            Font kitchenFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, shidhoriOrange);
            Paragraph kitchenTitle = new Paragraph("Shidhori Kitchen", kitchenFont);
            kitchenTitle.setAlignment(Element.ALIGN_CENTER);
            doc.add(kitchenTitle);

            Font invoiceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.GRAY);
            Paragraph invoiceLabel = new Paragraph("Invoice", invoiceFont);
            invoiceLabel.setAlignment(Element.ALIGN_CENTER);
            doc.add(invoiceLabel);
            doc.add(Chunk.NEWLINE);

            // === Customer Info ===
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
            PdfPTable customerTable = new PdfPTable(1);
            customerTable.setWidthPercentage(100);

            PdfPCell customerCell = new PdfPCell();
            customerCell.setBackgroundColor(grayBg);
            customerCell.setPadding(10f);
            Paragraph customerDetails = new Paragraph(
                    "Customer: " + user.getUserName() + "\n" +
                            "Email: " + user.getEmail() + "\n" +
                            "Phone No: " + user.getPhoneNo() + "\n" +
                            "Date: " + java.time.LocalDate.now(),
                    normalFont
            );
            customerDetails.setAlignment(Element.ALIGN_LEFT);
            customerCell.addElement(customerDetails);
            customerTable.addCell(customerCell);
            doc.add(customerTable);
            doc.add(Chunk.NEWLINE);

            // === Table ===
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.5f, 1.5f, 1.5f});
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            // === Table Header ===
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);

            Stream.of("Meal", "Price", "Quantity", "Total").forEach(columnTitle -> {
                PdfPCell header = new PdfPCell(new Phrase(columnTitle, headerFont));
                header.setBackgroundColor(headerBg2);
                header.setHorizontalAlignment(Element.ALIGN_CENTER);
                header.setPadding(8f);
                table.addCell(header);
            });

            double grandTotal = 0.0;

            // === Table Data ===
            for (OrderItem item : orderItems) {

                Order order = orderRepository.findById(item.getOrder().getOrderId()).get();

                if (!order.getOrderStatus().equals("DELIVERED")) {
                    throw new RuntimeException("Status is PENDING Or SOMETHING");
                }

                CloudKitchenMeal meal = cloudKitchenMealRepository
                        .findById(item.getCloudKitchenMeal().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Meal not found"));

                String mealName = meal.getMeal().getName();
                String mealPhotoUrl = meal.getMeal().getPhotos();
                double price = meal.getMeal().getPrice();
                int quantity = item.getQuantity();
                double lineTotal = item.getPrice();
                grandTotal += lineTotal;

                // === Meal (Image + Name)
                PdfPCell mealCell = createMealCell(mealName, mealPhotoUrl, normalFont, shidhoriOrange);
                table.addCell(mealCell);

                // === Price
                PdfPCell priceCell = new PdfPCell(new Phrase(String.format("₹ %.2f", price), normalFont));
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                priceCell.setPadding(6f);
                table.addCell(priceCell);

                // === Quantity
                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(quantity), normalFont));
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                qtyCell.setPadding(6f);
                table.addCell(qtyCell);

                // === Total
                PdfPCell totalCell = new PdfPCell(new Phrase(String.format("₹ %.2f", lineTotal), normalFont));
                totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                totalCell.setPadding(6f);
                table.addCell(totalCell);
            }

            doc.add(table);

            // === Grand Total ===
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
            Paragraph totalParagraph = new Paragraph("Grand Total: ₹ " + String.format("%.2f", grandTotal), totalFont);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            totalParagraph.setSpacingBefore(10f);
            doc.add(totalParagraph);

            // === Thank You Footer ===
            Font thankFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12, shidhoriOrange);
            Paragraph thankYou = new Paragraph("Thanks for dining with Shidhori Kitchen ❤️", thankFont);
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(30f);
            doc.add(thankYou);

            doc.close();
        } catch (Exception e) {
            throw new RuntimeException("Error creating invoice PDF", e);
        }
    }


    private PdfPCell createMealCell(String mealName, String mealPhotoUrl, Font font, Color borderColor) {
        try {
            Image mealImage = Image.getInstance(new URL(mealPhotoUrl));
            mealImage.scaleToFit(60, 60);
            mealImage.setBorder(Rectangle.BOX);
            mealImage.setBorderColor(borderColor);
            mealImage.setSpacingAfter(5f);

            Font mealNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Paragraph mealInfo = new Paragraph(mealName, mealNameFont);
            mealInfo.setAlignment(Element.ALIGN_CENTER);

            PdfPTable innerTable = new PdfPTable(1);
            innerTable.setWidthPercentage(100);

            PdfPCell imageCell = new PdfPCell(mealImage);
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            innerTable.addCell(imageCell);

            PdfPCell nameCell = new PdfPCell(mealInfo);
            nameCell.setBorder(Rectangle.NO_BORDER);
            nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            innerTable.addCell(nameCell);

            PdfPCell mealCell = new PdfPCell(innerTable);
            mealCell.setPadding(6f);
            return mealCell;

        } catch (Exception e) {
            PdfPCell fallbackCell = new PdfPCell(new Phrase(mealName, font));
            fallbackCell.setPadding(6f);
            return fallbackCell;
        }
    }


    @Override
    public Object getAllMealsByStateName(String stateName) {
        Object currentUserProfile = dataToken.getCurrentUserProfile();
        User user = (currentUserProfile instanceof User) ? (User) currentUserProfile : null;

        List<Cuisine> cuisines = cuisineRepository.findByState(stateName);

        if (cuisines == null || cuisines.isEmpty()) {
            log.warn("No cuisines found for state: {}", stateName);
            return Collections.emptyList();
        }

        List<Map<String, Object>> mapList = new ArrayList<>();
        List<CloudKitchenMeal> cloudKitchenMeals = cloudKitchenMealRepository.findAll();

        boolean hasActiveSubscription = false;
        if (user != null) {
            hasActiveSubscription = userSubscriptionRepository
                    .existsByUser_UserIdAndIsSubscribedTrue(user.getUserId());
        }

        LocalDate today = LocalDate.now();
        List<Offers> todayOffers = offersRepository.findByValidDate(today).stream()
                .filter(Offers::isActive)
                .toList();

        for (Cuisine cuisine : cuisines) {
            List<Meal> meals = cuisine.getMeals();
            if (meals == null || meals.isEmpty()) continue;

            for (Meal meal : meals) {
                for (CloudKitchenMeal kitchenMeal : cloudKitchenMeals) {
                    if (!kitchenMeal.getCloudKitchen().getIsDeleted()) {
                        if (!meal.getMealId().equals(kitchenMeal.getMeal().getMealId())) continue;
                        if (!kitchenMeal.isAvailable()) continue;

                        double originalPrice = meal.getPrice();
                        double finalPrice = originalPrice;

                        if (hasActiveSubscription) {
                            finalPrice = applyDiscount(originalPrice);
                        } else if (!todayOffers.isEmpty()) {
                            for (Offers offer : todayOffers) {
                                finalPrice = originalPrice * (1 - offer.getDiscountPercentage() / 100.0);
                            }
                        }

                        Map<String, Object> map = new HashMap<>();
                        map.put("mealId", kitchenMeal.getMeal().getMealId());
                        map.put("mealName", kitchenMeal.getMeal().getName());
                        map.put("photos", kitchenMeal.getMeal().getPhotos());
                        map.put("description", kitchenMeal.getMeal().getDescription());
                        map.put("nutritionalInformation", kitchenMeal.getMeal().getNutritionalInformation());
                        map.put("mealOriginalPrice", originalPrice);
                        map.put("mealFinalPrice", finalPrice);
                        map.put("cloudKitchenId", kitchenMeal.getCloudKitchen().getCloudKitchenId());
                        map.put("cloudKitchenName",
                                kitchenMeal.getCloudKitchen().getCity() + " - " + kitchenMeal.getCloudKitchen().getDivision());
                        map.put("hasSubscription", hasActiveSubscription);

                        mapList.add(map);
                    }
                }
            }
        }

        return mapList;
    }


    @Override
    public Object getAllCuisines() {
        return cuisineRepository.findAll();
    }

    @Override
    public Object getOffers() {
        User user = (User) dataToken.getCurrentUserProfile();

        boolean hasActiveSubscription = user != null &&
                userSubscriptionRepository.existsByUser_UserIdAndIsSubscribedTrue(user.getUserId());

        LocalDate today = LocalDate.now();
        List<Offers> todayOffers = offersRepository.findByValidDate(today);

        if (hasActiveSubscription || todayOffers.isEmpty()) {
            return "No offers available for you today.";
        }

        return todayOffers.stream()
                .filter(Offers::isActive)
                .map(offer -> "Today's offer: " + offer.getTitle() + " - " + offer.getDescription())
                .collect(Collectors.joining(" "));
    }

    @Override
    public Object getAllCloudKitchenName() {
        List<String> kitchenNameList = new ArrayList<>();
        List<CloudKitchen> cloudKitchens = cloudKitchenRepository.findByIsDeletedFalse();
        for (CloudKitchen cloudKitchen : cloudKitchens) {
            if (!cloudKitchen.getIsDeleted()) {
                kitchenNameList.add(cloudKitchen.getCity() + " - " + cloudKitchen.getDivision());
            }
        }
        return kitchenNameList;
    }

    @Override
    public Object getAllStateName() {
        return cuisineRepository.findAll();
    }


    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional
    public void generateMonthlyRandomOffer() {
        LocalDate today = LocalDate.now();

        int[] allowedDays = {7};
        Random random = new Random();
        int randomDay = allowedDays[random.nextInt(allowedDays.length)];

        LocalDate offerDate = today.withDayOfMonth(randomDay);

        boolean exists = offersRepository.existsByValidDateAndActiveTrue(offerDate);
        if (exists) {
            System.out.println("Offer already exists for " + offerDate);
            return;
        }

        Offers offer = new Offers();
        offer.setTitle("Monthly Special Day Offer");
        offer.setDescription("Get 20% off today only on meals!");
        offer.setDiscountPercentage(20);
        offer.setValidDate(offerDate);
        offer.setActive(true);

        offersRepository.save(offer);
        System.out.println("Monthly offer scheduled for: " + offerDate);
    }

}
