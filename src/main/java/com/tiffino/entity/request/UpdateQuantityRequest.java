package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQuantityRequest {

    private List<ItemQuantity> items;

    @Data
    public static class ItemQuantity {
        private Long mealId;
        private Integer quantity;
    }
}
