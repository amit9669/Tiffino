package com.tiffino.entity.request;

import com.tiffino.entity.DurationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionRequest {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private DurationType durationType;
}
