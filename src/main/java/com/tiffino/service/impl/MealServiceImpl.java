package com.tiffino.service.impl;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import com.tiffino.entity.request.MealRequest;
import com.tiffino.repository.CuisineRepository;
import com.tiffino.repository.MealRepository;
import com.tiffino.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MealServiceImpl implements MealService {

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Override
    public Object saveOrUpdateMeal(MealRequest mealRequest) {

        // 1. Check cuisine exists
        Cuisine cuisine = cuisineRepository.findById(mealRequest.getCuisineId())
                .orElseThrow(() -> new RuntimeException("Invalid Cuisine Id"));

        // 2. If Meal ID exists -> update
        if (mealRequest.getMealId() != null && mealRepository.existsById(mealRequest.getMealId())) {
            Meal meal = mealRepository.findById(mealRequest.getMealId())
                    .orElseThrow(() -> new RuntimeException("Meal not found"));

            meal.setName(mealRequest.getName());
            meal.setDescription(mealRequest.getDescription());
            meal.setNutritionalInformation(mealRequest.getNutritionalInformation());
            meal.setPrice(mealRequest.getPrice());
            meal.setCuisine(cuisine);
            meal.setUpdatedAt(LocalDateTime.now());

            if (mealRequest.getPhotos() != null && !mealRequest.getPhotos().isEmpty()) {
                meal.setPhotos(String.valueOf(mealRequest.getPhotos()));
            }

            mealRepository.save(meal);
            return "Meal Updated Successfully!!";

        } else {
            // 3. Insert new Meal
            Meal meal = new Meal();
            meal.setName(mealRequest.getName());
            meal.setDescription(mealRequest.getDescription());
            meal.setNutritionalInformation(mealRequest.getNutritionalInformation());
            meal.setPrice(mealRequest.getPrice());
            meal.setCuisine(cuisine);
            meal.setCreatedAt(LocalDateTime.now());
            meal.setUpdatedAt(LocalDateTime.now());

            if (mealRequest.getPhotos() != null && !mealRequest.getPhotos().isEmpty()) {
                meal.setPhotos(String.valueOf(mealRequest.getPhotos()));
            }

            mealRepository.save(meal);
            return "Meal Inserted Successfully!!";
        }
    }




}
