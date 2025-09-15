package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartRequest {

    private String cloudKitchenId;
    private List<CartMealItem> meals;

    @Data
    public static class CartMealItem {
        private Long mealId;
        private int quantity;
    }
}