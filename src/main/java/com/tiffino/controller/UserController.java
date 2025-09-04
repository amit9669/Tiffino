package com.tiffino.controller;

import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.service.EmailService;
import com.tiffino.service.IUserService;
import com.tiffino.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        return new ResponseEntity<>(iUserService.registerUser(userRegistrationRequest), HttpStatus.OK);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        if (!iUserService.checkUserExistsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        int otp = otpService.generateOTP(email);

        emailService.sendOtpEmail(email, otp);

        return ResponseEntity.ok("OTP has been sent to your email");
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

    @GetMapping("/getAllAvailableMealsGroupedByCuisine")
    public ResponseEntity<?> getAllAvailableMealsGroupedByCuisine() {
        return new ResponseEntity<>(iUserService.getAllAvailableMealsGroupedByCuisine(), HttpStatus.OK);
    }


    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdationRequest req) {
        return new ResponseEntity<>(iUserService.updateCurrentUser(req), HttpStatus.OK);
    }

    @GetMapping("/getAllSubscriptionPlan")
    public ResponseEntity<?> getAllSubscriptionPlan() {
        return new ResponseEntity<>(iUserService.getAllSubscriptionPlan(), HttpStatus.OK);
    }

    @PostMapping("/assignSubscriptionToUser")
    public ResponseEntity<?> assignSubscriptionToUser(@RequestParam String name, @RequestParam Double price) {
        return new ResponseEntity<>(iUserService.assignSubscriptionToUser(name, price), HttpStatus.OK);
    }

    @PostMapping("/redeemOffer/{offerId}")
    public ResponseEntity<?> redeemOffer(@PathVariable Long offerId) {
        return ResponseEntity.ok(iUserService.redeemOffer(offerId));
    }

    @GetMapping("/getUserAllOffers")
    public ResponseEntity<?> getUserAllOffers() {
        return new ResponseEntity<>(iUserService.getUserAllOffers(), HttpStatus.OK);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        return new ResponseEntity<>(iUserService.createOrder(request), HttpStatus.OK);
    }

    @DeleteMapping("/deleteOrder/{orderId}")
    public ResponseEntity<String> deleteOrder(@PathVariable Long orderId) {
        iUserService.deleteOrder(orderId);
        return ResponseEntity.ok("Order deleted successfully.");
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        return new ResponseEntity<>(iUserService.getAllOrders(), HttpStatus.OK);
    }

    @PostMapping("/createReview")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        return new ResponseEntity<>(iUserService.createReview(request), HttpStatus.OK);
    }

    @DeleteMapping("/deleteReview/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        iUserService.deleteReview(id);
        return ResponseEntity.ok("Review deleted successfully");
    }

    @GetMapping("/trackOrder")
    public ResponseEntity<?> trackOrder() {
        return new ResponseEntity<>(iUserService.trackOrder(), HttpStatus.OK);
    }
}