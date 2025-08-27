package com.tiffino.service;

import jakarta.servlet.http.HttpSession;

public interface IDeliveryPersonService {

    Object pickupOrder(Long deliveryId);

    Object deliverOrder(Long deliveryId);

    Object updatePassword(String managerId, int otp, String newPassword);

    Object forgotPasswordOfDeliveryPartner(String email, HttpSession session);

    Object changePassword(int otp, String newPassword, String confirmNewPassword, HttpSession session);
}
