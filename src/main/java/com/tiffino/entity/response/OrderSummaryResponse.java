package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryResponse {

    private Long orderId;
    private String orderStatus;
    private double totalCost;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String phoneNo;
    private String allergies;
}
