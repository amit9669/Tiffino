package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponseForManager {

    private Long orderId;
    private String orderStatus;
    private String userName;
    private String address;
    private Double totalCost;
    private String orderDate;
    private String orderTime;
}
