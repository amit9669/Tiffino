package com.tiffino.service.impl;


import com.tiffino.entity.Cuisine;
import com.tiffino.entity.request.CuisineRequest;
import com.tiffino.repository.CuisineRepository;
import com.tiffino.repository.MealRepository;
import com.tiffino.service.ICuisineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class CuisineService implements ICuisineService {

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private MealRepository mealRepository;

    @Override
    public String saveOrUpdateCuisine(CuisineRequest cuisineRequest) throws IOException {

        if (cuisineRequest.getCuisineId() != null && cuisineRepository.existsById(cuisineRequest.getCuisineId())) {
            Cuisine cuisine = cuisineRepository.findById(cuisineRequest.getCuisineId())
                    .orElseThrow(() -> new RuntimeException("Cuisine not found"));

            cuisine.setName(cuisineRequest.getName());
            cuisine.setDescription(cuisineRequest.getDescription());
            cuisine.setUpdatedAt(LocalDateTime.now());

            cuisineRepository.save(cuisine);
            return "Cuisine Updated Successfully!!";

        } else {

            Cuisine cuisine = new Cuisine();
            cuisine.setName(cuisineRequest.getName());
            cuisine.setDescription(cuisineRequest.getDescription());
            cuisine.setCreatedAt(LocalDateTime.now());
            cuisine.setUpdatedAt(LocalDateTime.now());

            cuisineRepository.save(cuisine);
            return "Cuisine Inserted Successfully!!";
        }
    }


    @Override
    public Object deleteCuisine(Long cuisineId) {
        if (!cuisineRepository.existsById(cuisineId)) {
            throw new RuntimeException("Cuisine not found with id " + cuisineId);
        }
        cuisineRepository.deleteById(cuisineId);
        return "Cuisine deleted successfully";
    }

    @Override
    public Object getCuisineById(Long cuisineId) {
        return cuisineRepository.findById(cuisineId)
                .orElseThrow(() -> new RuntimeException("Cuisine not found with id " + cuisineId));
    }

    @Override
    public Object getAllCuisines() {
        return cuisineRepository.findAll();
    }

    @Override
    public Object getAllCuisinesWithMeals() {
        return cuisineRepository.findAll();
    }

}
