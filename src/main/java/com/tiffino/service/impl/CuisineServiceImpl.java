package com.tiffino.service.impl;


import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import com.tiffino.entity.request.CuisineRequest;
import com.tiffino.repository.CuisineRepository;
import com.tiffino.repository.MealRepository;
import com.tiffino.service.CuisineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CuisineServiceImpl implements CuisineService {

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private MealRepository mealRepository;

    @Override
    public String saveOrUpdateCuisine(CuisineRequest cuisineRequest) throws IOException {

        // 1. If Cuisine ID exists -> update
        if (cuisineRequest.getCuisineId() != null && cuisineRepository.existsById(cuisineRequest.getCuisineId())) {
            Cuisine cuisine = cuisineRepository.findById(cuisineRequest.getCuisineId())
                    .orElseThrow(() -> new RuntimeException("Cuisine not found"));

            cuisine.setName(cuisineRequest.getName());
            cuisine.setDescription(cuisineRequest.getDescription());
            cuisine.setUpdatedAt(LocalDateTime.now());

            if (cuisineRequest.getImage() != null && !cuisineRequest.getImage().isEmpty()) {
                cuisine.setImage(String.valueOf(cuisineRequest.getImage()));
            }

            cuisineRepository.save(cuisine);
            return "Cuisine Updated Successfully!!";

        } else {
            // 2. Insert new Cuisine
            Cuisine cuisine = new Cuisine();
            cuisine.setName(cuisineRequest.getName());
            cuisine.setDescription(cuisineRequest.getDescription());
            cuisine.setCreatedAt(LocalDateTime.now());
            cuisine.setUpdatedAt(LocalDateTime.now());

            if (cuisineRequest.getImage() != null && !cuisineRequest.getImage().isEmpty()) {
                cuisine.setImage(String.valueOf(cuisineRequest.getImage()));
            }

            cuisineRepository.save(cuisine);
            return "Cuisine Inserted Successfully!!";
        }
    }


    @Override
    public String deleteCuisine(Long cuisineId) {
        if (!cuisineRepository.existsById(cuisineId)) {
            throw new RuntimeException("Cuisine not found with id " + cuisineId);
        }
        cuisineRepository.deleteById(cuisineId);
        return "Cuisine deleted successfully";
    }

    // --- GET SINGLE ---
    @Override
    public Cuisine getCuisineById(Long cuisineId) {
        return cuisineRepository.findById(cuisineId)
                .orElseThrow(() -> new RuntimeException("Cuisine not found with id " + cuisineId));
    }

    // --- GET ALL ---
    @Override
    public List<Cuisine> getAllCuisines() {
        return cuisineRepository.findAll();
    }

    @Override
    public List<Cuisine> getAllCuisinesWithMeals() {
        return cuisineRepository.findAll();
    }

}
