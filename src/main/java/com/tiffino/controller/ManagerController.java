package com.tiffino.controller;

import com.tiffino.entity.request.ManagerPasswordRequest;
import com.tiffino.service.IManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/getAllCuisinesAndMeals")
    public ResponseEntity<?> getAllCuisinesAndMeals() {
        return new ResponseEntity<>(iManagerService.getAllCuisinesAndMeals(), HttpStatus.OK);
    }

    @GetMapping("/getDataOfCloudKitchen")
    public ResponseEntity<?> getDataOfCloudKitchen() {
        return new ResponseEntity<>(iManagerService.getDataOfCloudKitchen(), HttpStatus.OK);
    }

    @PostMapping("/enableMealForKitchen/{mealId}")
    public ResponseEntity<?> enableMealForKitchen(@PathVariable Long mealId) {
        return new ResponseEntity<>(iManagerService.enableMealForKitchen(mealId), HttpStatus.OK);
    }

    @GetMapping("/getAllCloudKitchenMealIsAvailable")
    public ResponseEntity<?> getAllCloudKitchenMealIsAvailable() {
        return new ResponseEntity<>(iManagerService.getAllCloudKitchenMealIsAvailable(), HttpStatus.OK);
    }

    @PostMapping("/disableMealForKitchen/{mealId}")
    public ResponseEntity<?> disableMealForKitchen(@PathVariable Long mealId) {
        return new ResponseEntity<>(iManagerService.disableMealForKitchen(mealId), HttpStatus.OK);
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

    @GetMapping("/getAllDetails")
    public ResponseEntity<?> getAllDetails(){
        return new ResponseEntity<>(iManagerService.getAllDetails(),HttpStatus.OK);
    }

    @PostMapping("/acceptedOrder/{orderId}")
    public ResponseEntity<?> acceptedOrder(@PathVariable Long orderId){
        return new ResponseEntity<>(iManagerService.acceptedOrder(orderId),HttpStatus.OK);
    }

    @PostMapping("/orderPrepared/{orderId}")
    public ResponseEntity<?> orderPrepared(@PathVariable Long orderId){
        return new ResponseEntity<>(iManagerService.orderPrepared(orderId),HttpStatus.OK);
    }

    @PostMapping("/addOrRemoveMeals/{mealId}")
    public ResponseEntity<?> addOrRemoveMeals(@PathVariable Long mealId){
        return new ResponseEntity<>(iManagerService.addOrRemoveMeals(mealId),HttpStatus.OK);
    }
}
