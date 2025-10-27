package com.tiffino.controller;

import com.tiffino.entity.DeliveryDetails;
import com.tiffino.entity.DurationType;
import com.tiffino.entity.Order;
import com.tiffino.entity.request.*;
import com.tiffino.repository.OrderRepository;
import com.tiffino.service.EmailService;
import com.tiffino.service.IUserService;
import com.tiffino.service.OtpService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/user")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private OrderRepository orderRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {

        return new ResponseEntity<>(iUserService.registerUser(userRegistrationRequest), HttpStatus.OK);
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

    @GetMapping("/getAllCuisines")
    public ResponseEntity<?> getAllCuisines(){
        return new ResponseEntity<>(iUserService.getAllCuisines(),HttpStatus.OK);
    }

    @GetMapping("/getAllMealsByStateName/{stateName}")
    public ResponseEntity<?> getAllMealsByCuisineName(@PathVariable String stateName) {
        return new ResponseEntity<>(iUserService.getAllMealsByStateName(stateName), HttpStatus.OK);
    }

    @GetMapping("/getAllStateName")
    public ResponseEntity<?> getAllStateName(){
        return new ResponseEntity<>(iUserService.getAllStateName(),HttpStatus.OK);
    }

    @GetMapping("/getAllCloudKitchenName")
    public ResponseEntity<?> getAllCloudKitchenName(){
        return new ResponseEntity<>(iUserService.getAllCloudKitchenName(),HttpStatus.OK);
    }

    @PostMapping("/searchFilterForUser")
    public ResponseEntity<?> searchFilterForUser(@RequestBody Map<String, List<String>> request){
        List<String> cuisineNames = request.get("cuisineNames");
        List<String> cloudKitchenNames = request.get("cloudKitchenNames");
         return new ResponseEntity<>(iUserService.searchFilterForUser(cuisineNames, cloudKitchenNames),HttpStatus.OK);
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

    @PostMapping("/addAllergies")
    public ResponseEntity<?> addAllergies(@RequestBody List<String> allergies){
        return new ResponseEntity<>(iUserService.addAllergies(allergies),HttpStatus.OK);
    }

    @PostMapping("/updateCartQuantities")
    public ResponseEntity<?> updateCartQuantities(@RequestBody UpdateQuantityRequest request){
        return new ResponseEntity<>(iUserService.updateCartQuantities(request),HttpStatus.OK);
    }

    @GetMapping("/viewInvoice/{orderId}")
    public void viewInvoice(@PathVariable Long orderId, HttpServletResponse response) throws IOException {
        Order order = orderRepository.findById(orderId).get();
        if (!order.getOrderStatus().equals("DELIVERED")){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invoice can only be downloaded after delivery!");
            return;
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
        iUserService.viewInvoice(orderId, response.getOutputStream());
    }

    @GetMapping("/getOffers")
    public ResponseEntity<?> getOffers(){
        return new ResponseEntity<>(iUserService.getOffers(),HttpStatus.OK);
    }
}