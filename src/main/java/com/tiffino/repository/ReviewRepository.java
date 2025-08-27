package com.tiffino.repository;

import com.tiffino.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

//    // Find reviews by meal id
//    List<Review> findByMealMealId(Long mealId);

    // Find reviews by user id
    List<Review> findByUserUserId(Long userId);

    boolean existsByOrder_OrderId(Long orderId);
}
