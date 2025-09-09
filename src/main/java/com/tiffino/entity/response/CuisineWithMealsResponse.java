package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CuisineWithMealsResponse {

    private String cuisineName;
    private List<MealSummaryResponse> meals;
}
