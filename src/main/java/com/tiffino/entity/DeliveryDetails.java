package com.tiffino.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryDetails {

    private String address;
    private String state;
    private String city;
    private String pinCode;
    private String phoneNo;
    private String allergies;
}
