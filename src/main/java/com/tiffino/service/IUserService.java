package com.tiffino.service;

import com.tiffino.entity.Order;
import com.tiffino.entity.request.CreateOrderRequest;

public interface IUserService {

    public void registerUser(String name, String email, String password, String phoneNo);

    Order createOrder(CreateOrderRequest request);


}
