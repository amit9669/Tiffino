package com.tiffino.repository;
import com.tiffino.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findAllByUser_UserId(Long userId);

    Optional<Order> findByOrderIdAndUser_UserId(Long orderId, Long userId);

    List<Order> findAllByIsAvailableTrue();
}
