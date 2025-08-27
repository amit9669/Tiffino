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
    private String cloudKitchenId;
    private List<Long> mealIds;
    private DeliveryDetails deliveryDetails;
}
