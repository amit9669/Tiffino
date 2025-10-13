package com.tiffino.service;

public interface IDeliveryPersonService {

    Object pickupOrder(Long deliveryId);

    Object deliverOrder(Long deliveryId);

    Object updatePassword(String managerId, int otp, String newPassword);
}
