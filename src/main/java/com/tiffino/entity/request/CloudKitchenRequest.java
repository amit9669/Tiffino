package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CloudKitchenRequest {

    private String state;
    private String city;
    private String division;
    private String address;
    private Integer pinCode;
}
