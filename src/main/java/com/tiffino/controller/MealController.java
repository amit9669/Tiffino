package com.tiffino.controller;

import com.tiffino.entity.request.MealRequest;
import com.tiffino.service.MealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/meals")
public class MealController {

    @Autowired
    private MealService mealService;

    @PostMapping("/saveOrUpdate")
    public ResponseEntity<Object> saveOrUpdateMeal(@ModelAttribute MealRequest mealRequest){
        return ResponseEntity.ok(mealService.saveOrUpdateMeal(mealRequest));
    }

}
