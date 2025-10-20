package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseForManager {

    private Long orderId;
    private String orderStatus;
    private String userName;
    private String address;
    private String allergies;
    private Double totalCost;
    private String orderDate;
    private String orderTime;
    private List<String> mealName;
}
