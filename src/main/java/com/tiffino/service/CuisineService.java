package com.tiffino.service;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import com.tiffino.entity.request.CuisineRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface CuisineService {

    // --- CREATE / UPDATE ---
    String saveOrUpdateCuisine(CuisineRequest cuisineRequest) throws IOException;

    // --- DELETE ---
    String deleteCuisine(Long cuisineId);

    // --- GET SINGLE CUISINE ---
    Cuisine getCuisineById(Long cuisineId);

    // --- GET ALL CUISINES ---
    List<Cuisine> getAllCuisines();

    List<Cuisine> getAllCuisinesWithMeals();

}
