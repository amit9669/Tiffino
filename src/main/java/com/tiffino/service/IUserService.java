package com.tiffino.service;

import com.tiffino.entity.DeliveryDetails;
import com.tiffino.entity.request.*;

import java.io.OutputStream;


public interface IUserService {

    public Object registerUser(UserRegistrationRequest request);

    Object getAllAvailableMealsGroupedByCuisine();

    Object updateCurrentUser(UserUpdationRequest req);

    Object createOrder(DeliveryDetails deliveryDetails);

    Object getAllOrders();

    void deleteOrder(Long orderId);

    Object createReview(ReviewRequest request);

    void deleteReview(Long reviewId);

    public Object trackOrder(Long orderId);

    Object getAllMealsByCuisineName(String cuisineName);

    Object assignSubscriptionToUser(SubscriptionRequest subscriptionRequest);

    Object getAllGiftCardsOfUser();

    Object addMealsToCart(CartRequest request);

    Object removeMealFromCart(Long mealId);

    Object viewCart();

   Object updateCartQuantities(UpdateQuantityRequest request);

    void viewInvoice(Long orderId, OutputStream out);
}

