package com.tiffino.controller;

import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.service.IUserService;
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

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationRequest userRegistrationRequest) {

        iUserService.registerUser(userRegistrationRequest.getName(), userRegistrationRequest.getEmail(), userRegistrationRequest.getPassword(), userRegistrationRequest.getPhoneNo());
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/getAllSubscriptionPlan")
    public ResponseEntity<?> getAllSubscriptionPlan(){
        return new ResponseEntity<>(iUserService.getAllSubscriptionPlan(), HttpStatus.OK);
    }

    @PostMapping("/assignSubscriptionToUser")
    public ResponseEntity<?> assignSubscriptionToUser(@RequestParam String name, @RequestParam Double price){
        return new ResponseEntity<>(iUserService.assignSubscriptionToUser(name,price),HttpStatus.OK);
    }
}