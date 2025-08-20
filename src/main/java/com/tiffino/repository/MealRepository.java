package com.tiffino.repository;

import com.tiffino.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MealRepository extends JpaRepository<Meal, Long> {

    List<Meal> findByCuisine_CuisineId(Long cuisineId);

    List<Meal> findByCuisine_CuisineIdAndAvailableTrue(Long cuisineId);
}
