package com.tiffino.service;

import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface IManagerService {

    Object updatePassword(String managerId, int otp, String newPassword);

    Object getAllCuisinesAndMeals();

    Object getDataOfCloudKitchen();

    Object enableMealForKitchen(Long mealId);

    Object getAllCloudKitchenMealIsAvailable();

    Object disableMealForKitchen(Long mealId);

    Object assignOrderToDeliveryPerson(Long orderId, Long deliveryPersonId);

    Object getAllOrders();

    Object listOfDeliveryPersonIsAvailable();

    Object addOrRemoveMeals(Long mealId);

    Object acceptedOrder(Long orderId);

    Object orderPrepared(Long orderId);

    Object getAllDetails();

    Object openClosedCloudKitchen();

    Object getAllOrderQuery();
}
