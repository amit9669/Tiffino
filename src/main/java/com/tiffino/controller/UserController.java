package com.tiffino.controller;

import com.tiffino.entity.Order;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.entity.response.UserUpdationResponse;
import com.tiffino.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService iUserService;

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
}