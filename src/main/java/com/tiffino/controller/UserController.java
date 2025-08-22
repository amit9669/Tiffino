package com.tiffino.controller;

import com.tiffino.entity.Order;
import com.tiffino.entity.Review;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.entity.response.UserUpdationResponse;
import com.tiffino.service.EmailService;
import com.tiffino.service.IUserService;
import com.tiffino.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {

        iUserService.registerUser(userRegistrationRequest.getName(), userRegistrationRequest.getEmail(), userRegistrationRequest.getPassword(), userRegistrationRequest.getPhoneNo());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdationRequest req) {
        return new ResponseEntity<>( iUserService.updateCurrentUser(req),HttpStatus.OK);
    }

    @GetMapping("/getAllSubscriptionPlan")
    public ResponseEntity<?> getAllSubscriptionPlan() {
        return new ResponseEntity<>(iUserService.getAllSubscriptionPlan(), HttpStatus.OK);
    }

    @PostMapping("/assignSubscriptionToUser")
    public ResponseEntity<?> assignSubscriptionToUser(@RequestParam String name, @RequestParam Double price) {
        return new ResponseEntity<>(iUserService.assignSubscriptionToUser(name, price), HttpStatus.OK);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        return new ResponseEntity<>(iUserService.createOrder(request),HttpStatus.OK);
    }


    @PutMapping("/order/{orderId}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long orderId,
                                             @RequestBody CreateOrderRequest updatedOrderRequest) {
        Order updatedOrder = iUserService.updateOrder(orderId, updatedOrderRequest);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        iUserService.deleteOrder(orderId);
        return ResponseEntity.ok("Order deleted successfully.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        if (!iUserService.checkUserExistsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        int otp = otpService.generateOTP(email);

        emailService.sendOtpEmail(email, otp); // actual email send

        return ResponseEntity.ok("OTP has been sent to your email");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam int otp) {
        int storedOtp = otpService.getOtp(email);
        return storedOtp == otp
                ? ResponseEntity.ok("OTP verified")
                : ResponseEntity.badRequest().body("Invalid OTP");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String email,
            @RequestParam int otp,
            @RequestParam String newPassword) {

        int storedOtp = otpService.getOtp(email);
        if (storedOtp != otp) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        iUserService.updatePasswordByEmail(email, newPassword);
        otpService.clearOTP(email);
        return ResponseEntity.ok("Password updated successfully");
    }


    @PostMapping("/createReview")
    public ResponseEntity<Review> createReview(@RequestBody ReviewRequest request) {
        return ResponseEntity.ok(iUserService.createReview(request));
    }

    @GetMapping("/getAllReviews")
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(iUserService.getAllReviews());
    }

    @GetMapping("/getReviewById/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(iUserService.getReviewById(id));
    }

    @PutMapping("/updateReview/{id}")
    public ResponseEntity<String> updateReview(@PathVariable Long id, @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(iUserService.updateReview(id, request));
    }

    @DeleteMapping("/deleteReview/{id}")
    public ResponseEntity<String> deleteReview(@PathVariable Long id) {
        iUserService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }


    @GetMapping("/getReviewsByUser/{userId}")
    public ResponseEntity<List<Review>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(iUserService.getReviewsByUserId(userId));
    }


}