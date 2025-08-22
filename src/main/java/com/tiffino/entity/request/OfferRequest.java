package com.tiffino.entity.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRequest {
    private String type;
    private String description;
    private String termsAndConditions;
}
