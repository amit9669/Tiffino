package com.tiffino.service;

import jakarta.servlet.http.HttpSession;

public interface IDeliveryPersonService {

    Object pickupOrder(Long deliveryId);

    Object deliverOrder(Long deliveryId);

    Object updatePassword(String managerId, int otp, String newPassword);
}
