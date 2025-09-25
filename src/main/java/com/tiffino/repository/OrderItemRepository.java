package com.tiffino.repository;

import com.tiffino.entity.Order;
import com.tiffino.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    OrderItem findByOrder_OrderId(Long orderId);

    List<OrderItem> findAllByOrder_OrderId(Long orderId);
}
