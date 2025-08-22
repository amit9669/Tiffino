package com.tiffino.service;

import com.tiffino.entity.Order;
import com.tiffino.entity.Review;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.entity.request.ReviewRequest;

import java.util.List;

public interface IUserService {

    public void registerUser(String name, String email, String password, String phoneNo);

    Object getAllSubscriptionPlan();

    Object assignSubscriptionToUser(String name, Double price);
    Order createOrder(CreateOrderRequest request);

    Order getOrderById(Long orderId);

    List<Order> getAllOrders();

    void deleteOrder(Long orderId);

    Order updateOrder(Long orderId, CreateOrderRequest updatedOrderRequest);

    boolean checkUserExistsByEmail(String email);

    void updatePasswordByEmail(String email, String newPassword);


    Review createReview(ReviewRequest request);


    String updateReview(Long id, ReviewRequest request);

    void deleteReview(Long reviewId);

    Review getReviewById(Long reviewId);

    List<Review> getAllReviews();

//    List<Review> getReviewsByMealId(Long mealId);

    List<Review> getReviewsByUserId(Long userId);
}
