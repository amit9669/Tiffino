package com.tiffino.service;

import com.tiffino.entity.DeliveryDetails;
import com.tiffino.entity.request.*;

import java.io.OutputStream;
import java.util.List;


public interface IUserService {

    public Object registerUser(UserRegistrationRequest request);

    Object getAllAvailableMealsGroupedByCuisine();

    Object updateCurrentUser(UserUpdationRequest req);

    Object createOrder(DeliveryDetails deliveryDetails);

    Object getAllOrders();

    void deleteOrder(Long orderId);

    Object createReview(ReviewRequest request);

    void deleteReview(Long reviewId);

    Object trackOrder(Long orderId);

    Object searchFilterForUser(List<String> cuisineNames,List<String> cloudKitchenName);

    Object assignSubscriptionToUser(SubscriptionRequest subscriptionRequest);

    Object getAllGiftCardsOfUser();

    Object addMealsToCart(CartRequest request);

    Object removeMealFromCart(Long mealId);

    Object viewCart();

   Object updateCartQuantities(UpdateQuantityRequest request);

    void viewInvoice(Long orderId, OutputStream out);

    Object getAllMealsByStateName(String stateName);

    Object getAllCuisines();
}

