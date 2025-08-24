package com.tiffino.service.impl;

import com.tiffino.entity.Delivery;
import com.tiffino.entity.DeliveryPerson;
import com.tiffino.entity.DeliveryStatus;
import com.tiffino.repository.DeliveryPersonRepository;
import com.tiffino.repository.DeliveryRepository;
import com.tiffino.service.IDeliveryPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DeliveryPersonService implements IDeliveryPersonService {
    
    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    @Override
    public Object pickupOrder(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.ASSIGNED) {
            throw new RuntimeException("Order cannot be picked up, current status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.PICKED_UP);
        delivery.setPickedUpAt(LocalDateTime.now());

        delivery.getOrder().setOrderStatus("OUT_FOR_DELIVERY"); // update order status

        deliveryRepository.save(delivery);
        return "OUT_FOR_DELIVERY";
    }

    @Override
    public Object deliverOrder(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new RuntimeException("Delivery not found"));

        if (delivery.getStatus() != DeliveryStatus.PICKED_UP) {
            throw new RuntimeException("Order cannot be delivered, current status: " + delivery.getStatus());
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveredAt(LocalDateTime.now());

        delivery.getOrder().setOrderStatus("DELIVERED");

        DeliveryPerson dp = delivery.getDeliveryPerson();
        dp.setIsAvailable(true);
        deliveryPersonRepository.save(dp);

         deliveryRepository.save(delivery);
        return "DELIVERED";
    }
}
