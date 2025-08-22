package com.tiffino.service;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import com.tiffino.entity.request.CuisineRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public interface ICuisineService {

    Object saveOrUpdateCuisine(CuisineRequest cuisineRequest) throws IOException;

    Object deleteCuisine(Long cuisineId);

    Object getCuisineById(Long cuisineId);

    Object getAllCuisines();

    Object getAllCuisinesWithMeals();

}
