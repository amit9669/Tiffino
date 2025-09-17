package com.tiffino.entity.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long cartId;
    private String cloudKitchenId;
    private String cloudKitchenName;
    private double totalPrice;
    private List<CartMealInfo> meals;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartMealInfo {
        private Long mealId;
        private String mealName;
        private String mealPhotos;
        private double unitPrice;
        private int quantity;
        private double lineTotal;
    }
}
