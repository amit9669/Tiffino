package com.tiffino.entity.request;

import com.tiffino.entity.DeliveryDetails;
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
    private DeliveryDetails deliveryDetails;
    private String Status;

    //change id get by token
}
