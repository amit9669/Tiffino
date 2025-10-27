package com.tiffino.controller;

import com.tiffino.entity.request.LogInRequest;
import com.tiffino.entity.response.LogInResponse;
import com.tiffino.service.AuthService;
import com.tiffino.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@SecurityRequirement(name = "bearerAuth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LogInRequest req) {

        return new ResponseEntity<>(authService.login(req.getEmailOrId(), req.getPassword()), HttpStatus.OK);
    }

    @GetMapping("/getProfile")
    public ResponseEntity<?> getProfile() {
        return new ResponseEntity<>(authService.getProfile(), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            return ResponseEntity.ok("Logged out successfully");
        }

        return ResponseEntity.badRequest().body("No token found in request");
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<?> forgotPassword(@RequestParam String emailOrId, HttpSession session){
        return new ResponseEntity<>(authService.forgotPassword(emailOrId,session),HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestParam int otp, @RequestParam String newPassword, @RequestParam String confirmNewPassword, HttpSession session){
        return new ResponseEntity<>(authService.changePassword(otp,newPassword,confirmNewPassword,session),HttpStatus.OK);
    }
}
