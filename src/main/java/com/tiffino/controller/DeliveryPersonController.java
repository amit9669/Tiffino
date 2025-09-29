package com.tiffino.controller;

import com.tiffino.entity.request.DeliveryPersonPasswordRequest;
import com.tiffino.entity.request.ManagerPasswordRequest;
import com.tiffino.entity.request.PasswordRequest;
import com.tiffino.service.IDeliveryPersonService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery-person")
public class DeliveryPersonController {

    @Autowired
    private IDeliveryPersonService iDeliveryPersonService;

    @PostMapping("/updatePassword")
    public ResponseEntity<?> updatePassword(@RequestBody DeliveryPersonPasswordRequest passwordRequest) {
        return new ResponseEntity<>(iDeliveryPersonService.updatePassword(passwordRequest.getEmail(),
                passwordRequest.getOtp(), passwordRequest.getNewPassword()), HttpStatus.OK);
    }

    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<?> pickup(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(iDeliveryPersonService.pickupOrder(deliveryId));
    }

    @PostMapping("/{deliveryId}/deliver")
    public ResponseEntity<?> deliver(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(iDeliveryPersonService.deliverOrder(deliveryId));
    }
}
