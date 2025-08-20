package com.tiffino.service.impl;

import com.tiffino.entity.Meal;
import com.tiffino.entity.Order;
import com.tiffino.entity.User;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.repository.MealRepository;
import com.tiffino.repository.OrderRepository;
import com.tiffino.repository.UserRepository;
import com.tiffino.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
   private OrderRepository orderRepository;
@Autowired
private JavaMailSender mailSender;

@Autowired
private MealRepository mealRepository;

    public void registerUser(String name, String email, String password, String phoneNo) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exsists");
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




}
