package com.tiffino.entity.response;

import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private String orderStatus;
    private Double totalCost;
    private String orderDate;
    private String allergies;
    private String orderTime;
    private List<OrderMealsResponse> orderMealsResponses;

    @Data
    public class OrderMealsResponse {

        private String mealName;
        private String mealPhotos;
        private Integer mealQuantity;
        private Double mealPrice;
    }

}
