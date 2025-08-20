package com.tiffino.entity.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Long userId;
    private List<Long> mealIds;
    private String deliveryDetails;
    private String status;
}
