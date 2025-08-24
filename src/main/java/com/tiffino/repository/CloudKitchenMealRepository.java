package com.tiffino.repository;

import com.tiffino.entity.CloudKitchen;
import com.tiffino.entity.CloudKitchenMeal;
import com.tiffino.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CloudKitchenMealRepository extends JpaRepository<CloudKitchenMeal,Long> {
    Optional<CloudKitchenMeal> findByCloudKitchenAndMeal(CloudKitchen cloudKitchen, Meal meal);

    List<CloudKitchenMeal> findByCloudKitchenAndAvailableTrue(CloudKitchen cloudKitchen);
}
