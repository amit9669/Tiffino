package com.tiffino.service.impl;

import com.tiffino.entity.Order;
import com.tiffino.entity.User;
import com.tiffino.entity.request.CreateOrderRequest;
import com.tiffino.repository.OrderRepository;
import com.tiffino.repository.UserRepository;
import com.tiffino.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
   private OrderRepository orderRepository;


    public void registerUser(String name, String email, String password, String phoneNo) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exsists");
        }

        User user = User.builder()
                .userName(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNo(phoneNo)
                .build();

        userRepository.save(user);
    }





    @Override
    public Order createOrder(CreateOrderRequest dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);


        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderRepository.save(order);
    }
}
