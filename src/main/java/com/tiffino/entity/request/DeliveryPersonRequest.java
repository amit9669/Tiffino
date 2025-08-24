package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPersonRequest {

    private Long deliveryPersonId;

    private String name;

    private String email;

    private String phoneNo;
}
