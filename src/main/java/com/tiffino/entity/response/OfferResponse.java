package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfferResponse {

    private Long offerId;
    private String title;
    private String description;
    private double discountPercentage;
    private LocalDate validDate;
}
