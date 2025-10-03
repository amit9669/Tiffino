package com.tiffino.repository;

import com.tiffino.entity.Cuisine;
import com.tiffino.entity.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuisineRepository extends JpaRepository<Cuisine, Long> {
    List<Cuisine> findByCuisineId(Long cuisineId);

    Cuisine findByState(String stateName);
}
