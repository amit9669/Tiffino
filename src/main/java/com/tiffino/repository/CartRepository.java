package com.tiffino.repository;

import com.tiffino.entity.Cart;
import com.tiffino.entity.CloudKitchen;
import com.tiffino.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndCloudKitchenAndItemsIsNotEmpty(User user, CloudKitchen cloudKitchen);
    Optional<Cart> findByUser(User user);
}