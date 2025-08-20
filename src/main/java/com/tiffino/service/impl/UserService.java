package com.tiffino.service.impl;

import com.tiffino.entity.*;
import com.tiffino.entity.request.CreateOrderRequest;
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

import static org.springframework.http.HttpStatus.CONFLICT;
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
    public Order createOrder(CreateOrderRequest request) {
        // 1. Fetch user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Fetch meals
        List<Meal> meals = mealRepository.findAllById(request.getMealIds());

        if (meals.isEmpty()) {
            throw new RuntimeException("No meals found for given IDs");
        }

        // 3. Calculate total price
        double totalCost = meals.stream()
                .mapToDouble(Meal::getPrice)
                .sum();

        // 4. Build order
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

        // 5. Save order
        return orderRepository.save(order);
    }



    @Override
    @Transactional
    public UserUpdationResponse updateCurrentUser(String currentEmail, UserUpdationRequest req) {
        // Load the current user (auth guaranteed by security; this is the source of truth)
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        // Track changes (field → new value)
        Map<String, Object> updated = new LinkedHashMap<>();
        List<String> changed = new ArrayList<>();
        boolean passwordChanged = false;

        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (!Objects.equals(user.getUserName(), newName)) {
                user.setUserName(newName);
                updated.put("name", newName);
                changed.add("name");
            }
        }

        if (req.getPhoneNo() != null) {
            if (!Objects.equals(user.getPhoneNo(), req.getPhoneNo())) {
                user.setPhoneNo(req.getPhoneNo());
                updated.put("phoneNo", req.getPhoneNo());
                changed.add("phoneNo");
            }
        }

        if (req.getAddress() != null) {
            if (!Objects.equals(user.getAddress(), req.getAddress())) {
                user.setAddress(req.getAddress());
                updated.put("address", req.getAddress());
                changed.add("address");
            }
        }

        if (req.getMealPreference() != null) {
            if (!Objects.equals(user.getMealPreference(), req.getMealPreference())) {
                user.setMealPreference(req.getMealPreference());
                updated.put("mealPreference", req.getMealPreference());
                changed.add("mealPreference");
            }
        }

        if (req.getDietaryNeeds() != null) {
            if (!Objects.equals(user.getDietaryNeeds(), req.getDietaryNeeds())) {
                user.setDietaryNeeds(req.getDietaryNeeds());
                updated.put("dietaryNeeds", req.getDietaryNeeds());
                changed.add("dietaryNeeds");
            }
        }



        // Persist only once
        if (!changed.isEmpty()) {
            userRepository.save(user);
        }

        return UserUpdationResponse.builder()
                .updated(updated)              // only fields that changed, with new values
                .changedFields(changed)        // list of changed field names
                .build();
    }



}


