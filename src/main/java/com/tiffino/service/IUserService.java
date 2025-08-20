package com.tiffino.service;

import com.tiffino.entity.Order;
import com.tiffino.entity.request.CreateOrderRequest;

import com.tiffino.entity.User;
import com.tiffino.entity.request.UserUpdationRequest;
import com.tiffino.entity.response.UserUpdationResponse;

public interface IUserService {

    public void registerUser(String name, String email, String password, String phoneNo);
    Object getAllSubscriptionPlan();
    Object assignSubscriptionToUser(String name, Double price);
    Order createOrder(CreateOrderRequest request);
    UserUpdationResponse updateCurrentUser(String currentEmail, UserUpdationRequest req);

}

