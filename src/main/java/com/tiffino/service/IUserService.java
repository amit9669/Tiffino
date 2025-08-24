package com.tiffino.service;

import com.tiffino.entity.Order;
import com.tiffino.entity.Review;
import com.tiffino.entity.request.CreateOrderRequest;

import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.request.UserUpdationRequest;

import java.util.List;

public interface IUserService {

    public void registerUser(String name, String email, String password, String phoneNo);

    Object getAllSubscriptionPlan();

    Object assignSubscriptionToUser(String name, Double price);

    Object updateCurrentUser(UserUpdationRequest req);

    Object createOrder(CreateOrderRequest request);

    Object getOrderById(Long orderId);

    Object getAllOrders();

    void deleteOrder(Long orderId);

    Object createReview(ReviewRequest request);

    void deleteReview(Long reviewId);

    Object getReviewById(Long reviewId);

    Object getAllReviews();

    Object getReviewsByUserId();

    void updatePasswordByEmail(String email, String newPassword);

    boolean checkUserExistsByEmail(String email);

    Object trackOrder();
}

