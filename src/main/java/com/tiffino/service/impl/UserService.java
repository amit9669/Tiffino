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
}


