package com.tiffino.entity.response;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MealSummaryResponse {
    private Long mealId;
    private String mealName;
    private Boolean isSelected = false;
}