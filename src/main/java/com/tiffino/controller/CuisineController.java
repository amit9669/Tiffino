package com.tiffino.controller;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.request.CuisineRequest;
import com.tiffino.service.CuisineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/cuisines")
public class CuisineController {

    @Autowired
    private CuisineService cuisineService;


    @PostMapping("/save")
    public ResponseEntity<String> saveOrUpdateMeal(@ModelAttribute CuisineRequest cuisineRequest) throws IOException {
        return ResponseEntity.ok(cuisineService.saveOrUpdateCuisine(cuisineRequest));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCuisine(@PathVariable Long id) {
        return ResponseEntity.ok(cuisineService.deleteCuisine(id));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Cuisine> getCuisineById(@PathVariable Long id) {
        return ResponseEntity.ok(cuisineService.getCuisineById(id));
    }


    @GetMapping
    public ResponseEntity<List<Cuisine>> getAllCuisines() {
        return ResponseEntity.ok(cuisineService.getAllCuisines());
    }



    @GetMapping("/with-meals")
    public ResponseEntity<List<Cuisine>> getAllCuisinesWithMeals() {
        return ResponseEntity.ok(cuisineService.getAllCuisinesWithMeals());
    }

}
