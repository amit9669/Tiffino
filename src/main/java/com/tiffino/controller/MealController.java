package com.tiffino.controller;

import com.tiffino.entity.Meal;
import com.tiffino.entity.request.MealRequest;
import com.tiffino.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<Object> saveOrUpdateMeal(@ModelAttribute MealRequest mealRequest, Long cuisineId){
        return ResponseEntity.ok(mealService.saveOrUpdateMeal(mealRequest, cuisineId));
    }

    @GetMapping("/{id}/meals")
    public ResponseEntity<List<Meal>> getMealsByCuisine(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getMealsByCuisine(id));
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<List<Meal>> getAvailableMealsByCuisine(@PathVariable Long id) {
        return ResponseEntity.ok(mealService.getAvailableMealsByCuisine(id));
    }

}
