package com.tiffino.controller;

import com.tiffino.entity.Offer;
import com.tiffino.entity.request.*;
import com.tiffino.repository.OfferRepository;
import com.tiffino.service.ISuperAdminService;
import com.tiffino.service.impl.SuperAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/superAdmin")
public class SuperAdminController {

    @Autowired
    private ISuperAdminService iSuperAdminService;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private SuperAdminService superAdminService;

    @PostMapping("/saveOrUpdateAdmin")
    public ResponseEntity<?> saveOrUpdateAdmin(@RequestBody SuperAdminRequest superAdminRequest){
        return new ResponseEntity<>(iSuperAdminService.saveOrUpdateAdmin(superAdminRequest), HttpStatus.CREATED);
    }

    @PostMapping("/saveOrUpdateCloudKitchen")
    public ResponseEntity<?> saveOrUpdateCloudKitchen(@RequestBody CloudKitchenRequest kitchenRequest){
        return new ResponseEntity<>(iSuperAdminService.saveOrUpdateCloudKitchen(kitchenRequest),HttpStatus.CREATED);
    }

    @PostMapping("/saveOrUpdateManager")
    public ResponseEntity<?> saveOrUpdateManager(@ModelAttribute ManagerRequest managerRequest){
        return new ResponseEntity<>(iSuperAdminService.saveOrUpdateManager(managerRequest),HttpStatus.CREATED);
    }

    @PostMapping("/deleteCloudKitchen/{kitchenId}")
    public ResponseEntity<?> deleteCloudKitchen(@PathVariable String kitchenId){
        return new ResponseEntity<>(iSuperAdminService.deleteCloudKitchen(kitchenId),HttpStatus.OK);
    }

    @PostMapping("/deleteManager/{managerId}")
    public ResponseEntity<?> deleteManager(@PathVariable String managerId){
        return new ResponseEntity<>(iSuperAdminService.deleteManager(managerId),HttpStatus.OK);
    }


    @GetMapping("/searchFilterForAdmin")
    public ResponseEntity<?> searchFilterForAdmin(@RequestBody AdminFilterRequest adminFilterRequest){
        return new ResponseEntity<>(iSuperAdminService.searchFilterForAdmin(adminFilterRequest.getState(),
                adminFilterRequest.getCity(), adminFilterRequest.getDivision()),HttpStatus.FOUND);
    }

    @PostMapping("/saveOrUpdateSubscriptionPlan")
    public ResponseEntity<?> saveOrUpdateSubscriptionPlan(@RequestBody SubscriptionRequest subscriptionRequest){
        return new ResponseEntity<>(iSuperAdminService.saveOrUpdateSubscriptionPlan(subscriptionRequest),HttpStatus.CREATED);
    }
    @PostMapping("/createOffer")
    public ResponseEntity<?> createOffer(@RequestBody OfferRequest request) {
        return new ResponseEntity<>(iSuperAdminService.createOffer(request), HttpStatus.CREATED);
    }
}