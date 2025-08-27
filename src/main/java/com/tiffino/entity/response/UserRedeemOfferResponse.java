package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRedeemOfferResponse {

    private String userName;
    private String offerType;
    private String redeemDate;
    private String reedTime;
}
