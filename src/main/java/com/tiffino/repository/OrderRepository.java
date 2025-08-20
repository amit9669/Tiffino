package com.tiffino.repository;
import com.tiffino.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {

//    List<Order> findByUser_UserId(Long userId);
}
