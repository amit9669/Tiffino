package com.tiffino.controller;

import com.tiffino.entity.request.CuisineRequest;
import com.tiffino.service.ICuisineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/cuisines")
public class CuisineController {

    @Autowired
    private ICuisineService iCuisineService;


    @PostMapping("/saveOrUpdateCuisine")
    public ResponseEntity<?> saveOrUpdateCuisine(@RequestBody CuisineRequest cuisineRequest) throws IOException {
        return ResponseEntity.ok(iCuisineService.saveOrUpdateCuisine(cuisineRequest));
    }


    @DeleteMapping("/deleteCuisine/{id}")
    public ResponseEntity<?> deleteCuisine(@PathVariable Long id) {
        return ResponseEntity.ok(iCuisineService.deleteCuisine(id));
    }


    @GetMapping("/getCuisineById/{id}")
    public ResponseEntity<?> getCuisineById(@PathVariable Long id) {
        return ResponseEntity.ok(iCuisineService.getCuisineById(id));
    }


    @GetMapping("/getAllCuisines")
    public ResponseEntity<?> getAllCuisines() {
        return ResponseEntity.ok(iCuisineService.getAllCuisines());
    }



    @GetMapping("/getAllCuisinesWithMeals")
    public ResponseEntity<?> getAllCuisinesWithMeals() {
        return ResponseEntity.ok(iCuisineService.getAllCuisinesWithMeals());
    }

}
