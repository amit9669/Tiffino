package com.tiffino.service.impl;

import com.tiffino.entity.*;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.*;
import com.tiffino.service.DataToken;
import com.tiffino.entity.User;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.entity.response.UserUpdationResponse;
import com.tiffino.repository.UserRepository;
import com.tiffino.service.IUserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public Object createOrder(CreateOrderRequest request) {
        User user = (User) dataToken.getCurrentUserProfile();

        List<Meal> meals = mealRepository.findAllById(request.getMealIds());

        if (meals.isEmpty()) {
            return "No meals found for given IDs";
        }

        double totalCost = meals.stream()
                .mapToDouble(Meal::getPrice)
                .sum();

        Order order = Order.builder()
                .user(user)
                .meals(meals)
                .orderDate(LocalDateTime.now())
                .orderStatus("PENDING")
                .deliveryDetails(request.getDeliveryDetails())
                .totalCost(totalCost)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderRepository.save(order);
        return "Order Successfully!!";
    }
    public Order getOrderById(Long orderId) {
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
    public List<Order> getAllOrders() {
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
    public Order updateOrder(Long orderId, CreateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // ✅ Update User
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));
            order.setUser(user);
        }

        // ✅ Update Meals
        if (request.getMealIds() != null && !request.getMealIds().isEmpty()) {
            List<Meal> meals = mealRepository.findAllById(request.getMealIds());
            if (meals.size() != request.getMealIds().size()) {
                throw new RuntimeException("One or more meals not found.");
            }
            order.setMeals(meals);
        }

        // ✅ Update Order Status
        if (request.getStatus() != null) {
            order.setOrderStatus(request.getStatus());
        }

        // ✅ Update Delivery Details (embedded object)
        if (request.getDeliveryDetails() != null) {
            order.setDeliveryDetails(request.getDeliveryDetails());
        }

        // ✅ Update timestamp
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }



    @Override
    public Review createReview(ReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<CloudKitchen> cloudKitchens = new HashSet<>();
        for (String id : request.getCloudKitchenIds()) {
            CloudKitchen kitchen = cloudKitchenRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Cloud kitchen not found: " + id));
            cloudKitchens.add(kitchen);
        }

        Review review = Review.builder()
                .comment(request.getComment())
                .rating(request.getRating())
                .user(user)
                .cloudKitchens(cloudKitchens)
                .build();

        return reviewRepository.save(review);
    }



    @Override
    public String updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            review.setUser(user);
        }

        if (request.getCloudKitchenIds() != null && !request.getCloudKitchenIds().isEmpty()) {
            Set<CloudKitchen> kitchens = new HashSet<>(
                    cloudKitchenRepository.findAllById(request.getCloudKitchenIds())
            );
            review.setCloudKitchens(kitchens);
        }

        reviewRepository.save(review);
        return "Review updated successfully";
    }


    @Override
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public Review getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
        //remove it
    }

    @Override
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }



    @Override
    public List<Review> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserUserId(userId);
    }

    @Override
    public boolean checkUserExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }


}
