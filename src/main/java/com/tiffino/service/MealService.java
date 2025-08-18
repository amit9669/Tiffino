package com.tiffino.service;

import com.tiffino.entity.Meal;
import com.tiffino.entity.request.MealRequest;
import org.springframework.stereotype.Service;

@Service
public interface MealService {

    Object saveOrUpdateMeal(MealRequest mealRequest);
}
