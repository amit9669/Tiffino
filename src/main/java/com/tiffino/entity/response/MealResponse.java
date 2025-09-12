package com.tiffino.entity.response;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MealResponse {
    private Long mealId;
    private String mealName;
    private double price;
    private String photos;
    private String nutritionalInformation;
    private String description;
    private List<CloudKitchenInfo> kitchens;
}
