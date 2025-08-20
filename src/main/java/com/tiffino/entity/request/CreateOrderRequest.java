package com.tiffino.entity.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
@Data
public class CreateOrderRequest {
    private Long userId;
    private List<Long> mealIds;
    private String deliveryDetails;
    private String status;
    //change id get by token
}
