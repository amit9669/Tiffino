package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InvoiceOfOrdersResponse {

    private String mealName;
    private Integer mealQuantities;
    private Double price;
    private Double totalPrice;
}
