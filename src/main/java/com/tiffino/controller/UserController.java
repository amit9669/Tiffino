package com.tiffino.controller;

import com.tiffino.entity.DeliveryDetails;
import com.tiffino.entity.DurationType;
import com.tiffino.entity.request.*;
import com.tiffino.service.EmailService;
import com.tiffino.service.IUserService;
import com.tiffino.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

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

    @PostMapping("/assignSubscriptionToUser")
    public ResponseEntity<?> assignSubscriptionToUser(
            @RequestParam("durationType") DurationType durationType,
            @RequestParam("mealTimes") Set<String> mealTimes,
            @RequestParam(value = "allergies", required = false) Set<String> allergies,
            @RequestParam(value = "caloriesPerMeal", required = false) Integer caloriesPerMeal,
            @RequestParam(value = "dietaryFilePath", required = false) MultipartFile dietaryFilePath,
            @RequestParam(value = "giftCardCodeInput", required = false) String giftCardCodeInput) {

        SubscriptionRequest subscriptionRequest = new SubscriptionRequest();
        subscriptionRequest.setDurationType(durationType);
        subscriptionRequest.setMealTimes(mealTimes);
        subscriptionRequest.setAllergies(allergies);
        subscriptionRequest.setCaloriesPerMeal(caloriesPerMeal);
        subscriptionRequest.setGiftCardCodeInput(giftCardCodeInput);
        subscriptionRequest.setDietaryFilePath(dietaryFilePath);

        return ResponseEntity.ok(iUserService.assignSubscriptionToUser(subscriptionRequest));
    }



    @GetMapping("/getAllGiftCardsOfUser")
    public ResponseEntity<?> getAllGiftCardsOfUser() {
        return new ResponseEntity<>(iUserService.getAllGiftCardsOfUser(), HttpStatus.OK);
    }

    @PostMapping("/updateUser")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdationRequest req) {
        return new ResponseEntity<>(iUserService.updateCurrentUser(req), HttpStatus.OK);
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody DeliveryDetails deliveryDetails) {
        return new ResponseEntity<>(iUserService.createOrder(deliveryDetails), HttpStatus.OK);
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

    @GetMapping("/trackOrder/{orderId}")
    public ResponseEntity<?> trackOrder(@PathVariable Long orderId) {
        return new ResponseEntity<>(iUserService.trackOrder(orderId), HttpStatus.OK);
    }

    @GetMapping("/getAllMealsByCuisineName/{cuisineName}")
    public ResponseEntity<?> getAllMealsByCuisineName(@PathVariable String cuisineName) {
        return new ResponseEntity<>(iUserService.getAllMealsByCuisineName(cuisineName), HttpStatus.OK);
    }

    @PostMapping("/addCart")
    public ResponseEntity<?> addMultipleMeals(@RequestBody CartRequest request) {
        return ResponseEntity.ok(iUserService.addMealsToCart(request));
    }

    @DeleteMapping("/removeMeal/{mealId}")
    public ResponseEntity<?> removeMeal(@PathVariable Long mealId) {
        return ResponseEntity.ok(iUserService.removeMealFromCart(mealId));
    }

    @GetMapping("/viewCart")
    public ResponseEntity<?> viewCart() {
        return ResponseEntity.ok(iUserService.viewCart());
    }

    @PostMapping("/updateCartQuantities")
    public ResponseEntity<?> updateCartQuantities(@RequestBody UpdateQuantityRequest request){
        return new ResponseEntity<>(iUserService.updateCartQuantities(request),HttpStatus.OK);
    }

}