package com.tiffino.service.impl;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import com.tiffino.entity.request.MealRequest;
import com.tiffino.repository.CuisineRepository;
import com.tiffino.repository.MealRepository;
import com.tiffino.service.IMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MealService implements IMealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Override
    public Object saveOrUpdateMeal(MealRequest mealRequest) {

        Cuisine cuisine = cuisineRepository.findById(mealRequest.getCuisineId())
                .orElseThrow(() -> new RuntimeException("Invalid Cuisine Id"));

        if (mealRequest.getMealId() != null && mealRepository.existsById(mealRequest.getMealId())) {
            Meal meal = mealRepository.findById(mealRequest.getMealId())
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            meal.setName(mealRequest.getName());
            meal.setDescription(mealRequest.getDescription());
            meal.setNutritionalInformation(mealRequest.getNutritionalInformation());
            meal.setPrice(mealRequest.getPrice());
            meal.setCuisine(cuisine);
            meal.setUpdatedAt(LocalDateTime.now());

            mealRepository.save(meal);
            return "Meal Updated Successfully!!";

        } else {
            Meal meal = new Meal();
            meal.setName(mealRequest.getName());
            meal.setDescription(mealRequest.getDescription());
            meal.setNutritionalInformation(mealRequest.getNutritionalInformation());
            meal.setPrice(mealRequest.getPrice());
            meal.setCuisine(cuisine);
            meal.setCreatedAt(LocalDateTime.now());
            meal.setUpdatedAt(LocalDateTime.now());

            mealRepository.save(meal);
            return "Meal Inserted Successfully!!";
        }

    }

    @Override
    public List<Meal> getMealsByCuisine(Long cuisineId) {
        return mealRepository.findByCuisine_CuisineId(cuisineId);
    }

    @Override
    public List<Meal> getAvailableMealsByCuisine(Long cuisineId) {
        return mealRepository.findByCuisine_CuisineIdAndAvailableTrue(cuisineId);
    }
}
