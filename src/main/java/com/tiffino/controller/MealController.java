package com.tiffino.controller;

import com.tiffino.entity.request.MealRequest;
import com.tiffino.service.IMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/meals")
public class MealController {

    @Autowired
    private IMealService iMealService;

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<?> saveOrUpdateMeal(@RequestBody MealRequest mealRequest){
        return ResponseEntity.ok(iMealService.saveOrUpdateMeal(mealRequest));
    }

    @GetMapping("/{id}/meals")
    public ResponseEntity<?> getMealsByCuisine(@PathVariable Long id) {
        return ResponseEntity.ok(iMealService.getMealsByCuisine(id));
    }

    @GetMapping("/{id}/available")
    public ResponseEntity<?> getAvailableMealsByCuisine(@PathVariable Long id) {
        return ResponseEntity.ok(iMealService.getAvailableMealsByCuisine(id));
    }

}
