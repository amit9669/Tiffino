package com.tiffino.service;

import com.tiffino.entity.Order;
import com.tiffino.entity.Review;
import com.tiffino.entity.request.CreateOrderRequest;

import com.tiffino.entity.request.ReviewRequest;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.entity.response.UserUpdationResponse;

import java.util.List;

public interface IUserService {

    public void registerUser(String name, String email, String password, String phoneNo);

    Object getAllSubscriptionPlan();

    Object assignSubscriptionToUser(String name, Double price);


    Object updateCurrentUser(UserUpdationRequest req);

    Object createOrder (CreateOrderRequest request);
    Order getOrderById(Long orderId);

    List<Order> getAllOrders();

    void deleteOrder(Long orderId);

    Order updateOrder(Long orderId, CreateOrderRequest updatedOrderRequest);

    Review createReview(ReviewRequest request);


    String updateReview(Long id, ReviewRequest request);

    void deleteReview(Long reviewId);

    Review getReviewById(Long reviewId);

    List<Review> getAllReviews();

//    List<Review> getReviewsByMealId(Long mealId);

    List<Review> getReviewsByUserId(Long userId);

    void updatePasswordByEmail(String email, String newPassword);
    boolean checkUserExistsByEmail(String email);


}

