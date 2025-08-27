package com.tiffino.service;

import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface IManagerService {

    Object updatePassword(String managerId, int otp, String newPassword);

    Object forgotPasswordOfManager(String email, HttpSession session);

    Object changePassword(int otp, String newPassword, String confirmNewPassword, HttpSession session);

    Object getAllCuisinesAndMeals();

    Object getDataOfCloudKitchen();

    Object enableMealForKitchen(List<Long> mealIds);

    Object getAllCloudKitchenMealIsAvailable();

    Object disableMealForKitchen(List<Long> mealIds);

    Object assignOrderToDeliveryPerson(Long orderId, Long deliveryPersonId);

    Object getAllOrders();

    Object listOfDeliveryPersonIsAvailable();
}
