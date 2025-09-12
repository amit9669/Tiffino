package com.tiffino.controller;

import com.tiffino.entity.request.*;
import com.tiffino.repository.GiftCardsRepository;
import com.tiffino.service.ISuperAdminService;
import com.tiffino.service.impl.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/superAdmin")
public class SuperAdminController {

    @Autowired
    private ISuperAdminService iSuperAdminService;

    @Autowired
    private GiftCardsRepository giftCardsRepository;

    @Autowired
    private SuperAdminService superAdminService;

    @PostMapping("/updateAdmin")
    public ResponseEntity<?> updateAdmin(@RequestBody SuperAdminRequest superAdminRequest) {
        return new ResponseEntity<>(iSuperAdminService.updateAdmin(superAdminRequest), HttpStatus.OK);
    }

    @PostMapping("/saveCloudKitchen")
    public ResponseEntity<?> saveCloudKitchen(@RequestBody CloudKitchenRequest kitchenRequest) {
        return new ResponseEntity<>(iSuperAdminService.saveCloudKitchen(kitchenRequest), HttpStatus.CREATED);
    }

    @PostMapping("/saveManager")
    public ResponseEntity<?> saveManager(@ModelAttribute ManagerRequest managerRequest) {
        return new ResponseEntity<>(iSuperAdminService.saveManager(managerRequest), HttpStatus.CREATED);
    }

    @GetMapping("/getAllManagersWithCloudKitchen")
    public ResponseEntity<?> getAllManagersWithCloudKitchen() {
        return new ResponseEntity<>(iSuperAdminService.getAllManagersWithCloudKitchen(), HttpStatus.OK);
    }

    @PostMapping("/deleteCloudKitchen/{kitchenId}")
    public ResponseEntity<?> deleteCloudKitchen(@PathVariable String kitchenId) {
        return new ResponseEntity<>(iSuperAdminService.deleteCloudKitchen(kitchenId), HttpStatus.OK);
    }

    @PostMapping("/deleteManager/{managerId}")
    public ResponseEntity<?> deleteManager(@PathVariable String managerId) {
        return new ResponseEntity<>(iSuperAdminService.deleteManager(managerId), HttpStatus.OK);
    }

    @GetMapping("/searchFilterForAdmin")
    public ResponseEntity<?> searchFilterForAdmin(@RequestBody AdminFilterRequest adminFilterRequest) {
        return new ResponseEntity<>(iSuperAdminService.searchFilterForAdmin(adminFilterRequest.getState(),
                adminFilterRequest.getCity(), adminFilterRequest.getDivision()), HttpStatus.FOUND);
    }

    @PostMapping("/saveOrUpdateDeliveryPerson")
    public ResponseEntity<?> saveOrUpdateDeliveryPerson(@RequestBody DeliveryPersonRequest personRequest) {
        return new ResponseEntity<>(iSuperAdminService.saveOrUpdateDeliveryPerson(personRequest), HttpStatus.OK);
    }

    @PostMapping("/saveOrUpdateCuisine")
    public ResponseEntity<?> saveOrUpdateCuisine(@RequestBody CuisineRequest cuisineRequest) throws IOException {
        return ResponseEntity.ok(iSuperAdminService.saveOrUpdateCuisine(cuisineRequest));
    }

    @GetMapping("/getAllCuisines")
    public ResponseEntity<?> getAllCuisines(){
        return new ResponseEntity<>(iSuperAdminService.getAllCuisines(),HttpStatus.OK);
    }

    @PostMapping("/saveOrUpdateMeal")
    public ResponseEntity<?> saveOrUpdateMeal(
            @RequestParam("mealId") Long mealId,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("nutritionalInformation") String nutritionalInformation,
            @RequestParam("price") double price,
            @RequestParam("cuisineId") Long cuisineId,
            @RequestParam("photos") MultipartFile photos
    ) {
        MealRequest mealRequest = new MealRequest();
        mealRequest.setMealId(mealId);
        mealRequest.setName(name);
        mealRequest.setDescription(description);
        mealRequest.setNutritionalInformation(nutritionalInformation);
        mealRequest.setPrice(price);
        mealRequest.setCuisineId(cuisineId);
        mealRequest.setPhotos(photos);

        return ResponseEntity.ok(iSuperAdminService.saveOrUpdateMeal(mealRequest));
    }

    @GetMapping("/getAllSubscribedUser")
    public ResponseEntity<?> getAllSubscribedUser() {
        return new ResponseEntity<>(iSuperAdminService.getAllSubscribedUser(), HttpStatus.OK);
    }

    @GetMapping("/getAllCloudKItchenAndReviews")
    public ResponseEntity<?> getAllCloudKItchenAndReviews() {
        return new ResponseEntity<>(iSuperAdminService.getAllCloudKItchenAndReviews(), HttpStatus.OK);
    }
}