package com.tiffino.controller;

import com.tiffino.entity.Meal;
import com.tiffino.entity.Order;
import com.tiffino.entity.User;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.repository.MealRepository;
import com.tiffino.repository.OrderRepository;
import com.tiffino.repository.UserRepository;
import com.tiffino.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private MealRepository mealRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {

        iUserService.registerUser(userRegistrationRequest.getName(), userRegistrationRequest.getEmail(), userRegistrationRequest.getPassword(), userRegistrationRequest.getPhoneNo());
        return ResponseEntity.ok("User registered successfully");
    }



//    @PostMapping("/forgot-password-otp")
//    public ResponseEntity<?> sendOtp(@RequestParam String email) {
//        System.out.println("Sending OTP to email: " + email);
//        iUserService.sendOtp(email);
//        return ResponseEntity.ok("OTP sent to email.");
//    }
//
//    @PostMapping("/reset-password-otp")
//    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword) {
//        iUserService.resetPasswordWithOtp(email, otp, newPassword);
//        return ResponseEntity.ok("Password updated successfully.");
//    }

    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = iUserService.createOrder(request);
        return ResponseEntity.ok(order);
    }
}
