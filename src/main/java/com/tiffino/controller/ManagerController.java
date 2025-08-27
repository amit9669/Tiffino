package com.tiffino.controller;

import com.tiffino.entity.request.ManagerPasswordRequest;
import com.tiffino.entity.request.PasswordRequest;
import com.tiffino.service.IManagerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @Autowired
    private IManagerService iManagerService;

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody ManagerPasswordRequest passwordRequest) {
        return new ResponseEntity<>(iManagerService.updatePassword(passwordRequest.getManagerId(),
                passwordRequest.getOtp(), passwordRequest.getNewPassword()), HttpStatus.OK);
    }

    @PostMapping("/forgotPasswordOfManager")
    public ResponseEntity<?> forgotPasswordOfManager(@RequestParam String email, HttpSession session) {
        return new ResponseEntity<>(iManagerService.forgotPasswordOfManager(email, session), HttpStatus.OK);
    }

    @PostMapping("/changePassword")
    public ResponseEntity<?> changePassword(@RequestBody PasswordRequest passwordRequest, HttpSession session) {
        return new ResponseEntity<>(iManagerService.changePassword(passwordRequest.getOtp(), passwordRequest.getNewPassword(),
                passwordRequest.getConfirmNewPassword(), session), HttpStatus.OK);
    }

    @GetMapping("/getAllCuisinesAndMeals")
    public ResponseEntity<?> getAllCuisinesAndMeals() {
        return new ResponseEntity<>(iManagerService.getAllCuisinesAndMeals(), HttpStatus.OK);
    }

    @GetMapping("/getDataOfCloudKitchen")
    public ResponseEntity<?> getDataOfCloudKitchen() {
        return new ResponseEntity<>(iManagerService.getDataOfCloudKitchen(), HttpStatus.OK);
    }

    @PostMapping("/enableMealForKitchen")
    public ResponseEntity<?> enableMealForKitchen(@RequestBody List<Long> mealsIds) {
        return new ResponseEntity<>(iManagerService.enableMealForKitchen(mealsIds), HttpStatus.OK);
    }

    @GetMapping("/getAllCloudKitchenMealIsAvailable")
    public ResponseEntity<?> getAllCloudKitchenMealIsAvailable() {
        return new ResponseEntity<>(iManagerService.getAllCloudKitchenMealIsAvailable(), HttpStatus.OK);
    }

    @PostMapping("/disableMealForKitchen")
    public ResponseEntity<?> disableMealForKitchen(@RequestBody List<Long> mealsIds) {
        return new ResponseEntity<>(iManagerService.disableMealForKitchen(mealsIds), HttpStatus.OK);
    }

    @PostMapping("/assignOrderToDeliveryPerson")
    public ResponseEntity<?> assignOrderToDeliveryPerson(@RequestParam Long orderId, @RequestParam Long deliveryPersonId) {
        return new ResponseEntity<>(iManagerService.assignOrderToDeliveryPerson(orderId, deliveryPersonId), HttpStatus.OK);
    }

    @GetMapping("/listOfDeliveryPersonIsAvailable")
    public ResponseEntity<?> listOfDeliveryPersonIsAvailable() {
        return ResponseEntity.ok(iManagerService.listOfDeliveryPersonIsAvailable());
    }

    @GetMapping("/getAllOrders")
    public ResponseEntity<?> getAllOrders() {
        return new ResponseEntity<>(iManagerService.getAllOrders(), HttpStatus.OK);
    }
}
