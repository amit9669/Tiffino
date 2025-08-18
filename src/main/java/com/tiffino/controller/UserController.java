package com.tiffino.controller;

import com.tiffino.entity.Meal;
import com.tiffino.entity.Order;
import com.tiffino.entity.User;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.repository.OrderRepository;
import com.tiffino.repository.UserRepository;
import com.tiffino.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {

        iUserService.registerUser(userRegistrationRequest.getName(), userRegistrationRequest.getEmail(), userRegistrationRequest.getPassword(), userRegistrationRequest.getPhoneNo());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
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

        // 4. Build Order
        Order order = Order.builder()
                .user(user)
                .meals(meals)
                .orderDate(LocalDateTime.now())
                .orderStatus("PENDING")
                .deliveryDetails(request.getDeliveryDetails())

                .status(request.getStatus())
                .totalCost(totalCost)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 5. Save order
        Order savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(savedOrder);
    }



}
