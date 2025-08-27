package com.tiffino.repository;

import com.tiffino.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {
    List<Delivery> findAllByOrder_User_UserId(Long userId);

    Optional<Delivery> findByOrder_OrderId(Long orderId);
}
