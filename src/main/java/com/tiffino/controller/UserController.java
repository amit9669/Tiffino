package com.tiffino.controller;

import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
