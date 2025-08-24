package com.tiffino.service.impl;

import com.tiffino.entity.*;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.response.DeliveryTrackingResponse;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.*;
import com.tiffino.service.DataToken;
import com.tiffino.entity.User;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.repository.UserRepository;
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

    public void registerUser(String name, String email, String password, String phoneNo) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exits");
        }

        User user = User.builder()
                .userName(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNo(phoneNo)
                .build();

        userRepository.save(user);
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
                    "Please wait for expired date "+userSubscription.getExpiryDate();
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


    @Override
    public Object updateCurrentUser(UserUpdationRequest req) {
        User user = (User) dataToken.getCurrentUserProfile();
        user.setUserName(req.getName());
        user.setAddress(req.getAddress());
        user.setDietaryNeeds(req.getDietaryNeeds());
        user.setMealPreference(req.getMealPreference());
        user.setPhoneNo(req.getPhoneNo());
        userRepository.save(user);
        return "Updated Successfully!!";
    }

    @Override
    @Transactional
    public Object createOrder(CreateOrderRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = cloudKitchenRepository.findById(request.getCloudKitchenId())
                .orElseThrow(() -> new RuntimeException("CloudKitchen not found"));

        List<CloudKitchenMeal> kitchenMeals = cloudKitchenMealRepository
                .findByCloudKitchenAndAvailableTrue(cloudKitchen);

        Map<Long, Meal> availableMeals = kitchenMeals.stream()
                .map(CloudKitchenMeal::getMeal)
                .collect(Collectors.toMap(Meal::getMealId, m -> m));

        List<Meal> meals = new ArrayList<>();
        for (Long mealId : request.getMealIds()) {
            if (!availableMeals.containsKey(mealId)) {
                throw new RuntimeException("Meal ID " + mealId + " not available in this kitchen");
            }
            meals.add(availableMeals.get(mealId));
        }

        double totalCost = meals.stream()
                .mapToDouble(Meal::getPrice)
                .sum();

        Order order = Order.builder()
                .user(user)
                .cloudKitchen(cloudKitchen)
                .meals(meals)
                .orderStatus("PENDING")
                .deliveryDetails(request.getDeliveryDetails())
                .totalCost(totalCost)
                .build();

        orderRepository.save(order);
        return "Order placed successfully!";
    }


    public Object getOrderById(Long orderId) {
        return orderRepository.findById(orderId).map(order -> {
            if (order.getMeals() == null || order.getMeals().isEmpty()) {
                System.out.println("Meals are empty for order: " + orderId);
                throw new CustomException("Order has no meals");
            }
            return order;
        }).orElseThrow(() -> {
            System.out.println("Order not found for ID: " + orderId);
            return new CustomException("Order not found");
        });
    }

    @Override
    public Object getAllOrders() {
        return orderRepository.findAll();
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

        CloudKitchen cloudKitchen = cloudKitchenRepository.findById(request.getCloudKitchenId())
                .orElseThrow(() -> new RuntimeException("CloudKitchen not found"));

        Review review = Review.builder()
                .comment(request.getComment())
                .rating(request.getRating())
                .user(user)
                .cloudKitchen(cloudKitchen)
                .build();

        reviewRepository.save(review);
        return "Review submitted successfully!";
    }


    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Object getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
    }

    @Override
    public Object getAllReviews() {
        return reviewRepository.findAll();
    }


    @Override
    public Object getReviewsByUserId() {
        User user = (User) dataToken.getCurrentUserProfile();
        return reviewRepository.findByUserUserId(user.getUserId());
    }

    @Override
    public boolean checkUserExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Object trackOrder() {
        User user = (User) dataToken.getCurrentUserProfile();

        List<Delivery> deliveries = deliveryRepository.findAllByOrder_User_UserId(user.getUserId());

        if (deliveries.isEmpty()) {
            throw new RuntimeException("No pending deliveries found for this user");
        }
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

    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
