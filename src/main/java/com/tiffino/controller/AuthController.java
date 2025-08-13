package com.tiffino.controller;

import com.tiffino.entity.request.LogInRequest;
import com.tiffino.entity.response.LogInResponse;
import com.tiffino.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LogInRequest req) {
        String token = authService.login(req.getEmailOrId(), req.getPassword());
        return new ResponseEntity<>(LogInResponse.builder().jwtToken(token).build(), HttpStatus.OK);
    }

    @GetMapping("/getProfile")
    public ResponseEntity<?> getProfile(){
        return new ResponseEntity<>(authService.getProfile(),HttpStatus.FOUND);
    }
}
