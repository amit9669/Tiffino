package com.tiffino.service;

import com.tiffino.entity.request.MealRequest;
import org.springframework.stereotype.Service;

@Service
public interface IMealService {

    Object saveOrUpdateMeal(MealRequest mealRequest);

    Object getMealsByCuisine(Long cuisineId);

    Object getAvailableMealsByCuisine(Long cuisineId);
}
