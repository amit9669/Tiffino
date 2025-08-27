package com.tiffino.entity.response;

import com.tiffino.entity.UserOffer;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserOfferResponse {

    private Long userOfferId;   // unique ID of assignment
    private Long offerId;       // offer reference
    private String type;        // e.g. loyalty, discount, streak
    private String description;
    private Boolean isRedeemed;
    private LocalDateTime redeemedAt;

    public static UserOfferResponse fromEntity(UserOffer userOffer) {
        return UserOfferResponse.builder()
                .userOfferId(userOffer.getUserOfferId())
                .offerId(userOffer.getOffer().getOfferId())
                .type(userOffer.getOffer().getType())
                .description(userOffer.getOffer().getDescription())
                .isRedeemed(userOffer.getIsRedeemed())
                .redeemedAt(userOffer.getRedeemedAt())
                .build();
    }
}