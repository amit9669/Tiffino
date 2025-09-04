package com.tiffino.service;

import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.request.UserRegistrationRequest;
import com.tiffino.entity.request.UserUpdationRequest;


public interface IUserService {

    public Object registerUser(UserRegistrationRequest request);

    Object getAllAvailableMealsGroupedByCuisine();

    Object getAllSubscriptionPlan();

    Object assignSubscriptionToUser(String name, Double price);

    Object redeemOffer(Long offerId);

    Object getUserAllOffers();

    Object updateCurrentUser(UserUpdationRequest req);

    Object createOrder(CreateOrderRequest request);

    Object getAllOrders();

    void deleteOrder(Long orderId);

    Object createReview(ReviewRequest request);

    void deleteReview(Long reviewId);

    void updatePasswordByEmail(String email, String newPassword);

    boolean checkUserExistsByEmail(String email);

    Object trackOrder();
}

