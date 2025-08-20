package com.tiffino.service;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import com.tiffino.entity.request.MealRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface MealService {

    Object saveOrUpdateMeal(MealRequest mealRequest, Long cuisineId);

    List<Meal> getMealsByCuisine(Long cuisineId);

    List<Meal> getAvailableMealsByCuisine(Long cuisineId);
}
