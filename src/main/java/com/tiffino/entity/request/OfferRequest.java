package com.tiffino.entity.request;

import com.tiffino.entity.TargetGroup;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfferRequest {
    private String type;
    private String description;
    private String termsAndConditions;
    private Boolean isActive = true;
    private TargetGroup targetGroup;
    private Long subscriptionId;
}
