package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPersonRequest {

    private Long deliveryPersonId;
    private String name;
    private String email;
    private String phoneNo;
    private MultipartFile adharCard;
    private MultipartFile insurance;
    private MultipartFile licences;
    private String cloudKitchenId;
}
