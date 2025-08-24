package com.tiffino.controller;

import com.tiffino.service.IDeliveryPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/delivery-person")
public class DeliveryPersonController {

    @Autowired
    private IDeliveryPersonService iDeliveryPersonService;

    @PostMapping("/{deliveryId}/pickup")
    public ResponseEntity<?> pickup(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(iDeliveryPersonService.pickupOrder(deliveryId));
    }

    @PostMapping("/{deliveryId}/deliver")
    public ResponseEntity<?> deliver(@PathVariable Long deliveryId) {
        return ResponseEntity.ok(iDeliveryPersonService.deliverOrder(deliveryId));
    }
}
