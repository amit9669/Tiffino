package com.tiffino.service.impl;

import com.tiffino.entity.*;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.response.*;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.*;
import com.tiffino.service.DataToken;
import com.tiffino.entity.User;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.repository.UserRepository;
import com.tiffino.service.EmailService;
import com.tiffino.service.IUserService;
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
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private OrderRepository orderRepository;

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
    private UserOfferRepository userOfferRepository;

    @Autowired
    private EmailService emailService;


    public Object registerUser(String name, String email, String password, String phoneNo) {

        if (userRepository.findByEmail(email).isPresent()) {
            return "User already exits";
        }

        if (!emailService.isDeliverableEmail(email)) {
            return "Invalid or undeliverable email: " + email;
        }

        User user = User.builder()
                .userName(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNo(phoneNo)
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


    @Override
    public Object getAllSubscriptionPlan() {
        return subscriptionRepository.findAll();
    }

    @Override
    @Transactional
    public Object assignSubscriptionToUser(String name, Double price) {
        User user = (User) dataToken.getCurrentUserProfile();

        boolean alreadySubscribed = user.getSubscriptions().stream()
                .anyMatch(s -> s.getPlan().getSubName().equals(name)
                        && s.getPlan().getPrice().equals(price)
                        && Boolean.TRUE.equals(s.getIsSubscribed()));

        if (alreadySubscribed) {
            return "User already has this active subscription!";
        }

        Subscription plan = subscriptionRepository.findBySubNameAndPrice(name, price)
                .orElseThrow(() -> new CustomException("Subscription plan not available!!"));

        if (userSubscriptionRepository
                .existsByUser_UserIdAndIsSubscribedTrue(user.getUserId())) {
            UserSubscription userSubscription = userSubscriptionRepository.findByUser_UserIdAndIsSubscribedTrue(user.getUserId());
            return "User already has " + userSubscription.getPlan().getSubName() + " this active subscription! " +
                    "Please wait for expired date " + userSubscription.getExpiryDate();
        }

        UserSubscription userSubscription = UserSubscription.builder()
                .user(user)
                .plan(plan)
                .isSubscribed(true)
                .isDeleted(false)
                .startDate(LocalDateTime.now())
                .expiryDate(this.calculateExpiryDate(plan.getDurationType()))
                .build();

        user.getSubscriptions().add(userSubscription);
        userRepository.save(user);

        return "Subscribed Successfully!!!";
    }

    @Override
    @Transactional
    public Object redeemOffer(Long offerId) {
        User user = (User) dataToken.getCurrentUserProfile();
        Optional<UserOffer> userOfferOptional = userOfferRepository.findByUser_UserIdAndOffer_OfferId(user.getUserId(), offerId);

        if (!userOfferOptional.isPresent()) {
            return "Offer not assigned";
        }

        UserOffer userOffer = userOfferOptional.get();

        if (Boolean.TRUE.equals(userOffer.getIsRedeemed())) {
            return "Offer already redeemed";
        }

        userOffer.setIsRedeemed(true);
        userOffer.setRedeemedAt(LocalDateTime.now());

        return UserOfferResponse.fromEntity(userOfferRepository.save(userOffer));
    }


    @Override
    public List<UserOfferResponse> getUserAllOffers() {
        User user = (User) dataToken.getCurrentUserProfile();

        return userOfferRepository.findByUser_UserId(user.getUserId())
                .stream()
                .map(UserOfferResponse::fromEntity)
                .toList();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireSubscriptions() {
        List<UserSubscription> activeSubs = userSubscriptionRepository.findByIsSubscribedTrue();
        LocalDateTime now = LocalDateTime.now();

        for (UserSubscription us : activeSubs) {
            if (us.getExpiryDate().isBefore(now)) {
                us.setIsSubscribed(false);
                us.setIsDeleted(true);
                userSubscriptionRepository.save(us);
                System.out.println("Subscription expired for userId=" + us.getUser().getUserId());
            }
        }
    }

    private LocalDateTime calculateExpiryDate(DurationType durationType) {
        LocalDateTime now = LocalDateTime.now();
        switch (durationType) {
            case ONE_DAY:
                return now.plusDays(1);
            case WEEKLY:
                return now.plusWeeks(1);
            case MONTHLY:
                return now.plusMonths(1);
            case QUARTERLY:
                return now.plusMonths(3);
            case YEARLY:
                return now.plusYears(1);
            default:
                throw new CustomException("Invalid subscription duration type!");
        }
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
    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRole(Role.USER);
        userRepository.save(user);
    }
}
