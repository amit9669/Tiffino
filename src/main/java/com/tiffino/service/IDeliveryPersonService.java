package com.tiffino.service;

public interface IDeliveryPersonService {

    Object pickupOrder(Long deliveryId);

    Object deliverOrder(Long deliveryId);
}
