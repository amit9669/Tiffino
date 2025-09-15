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
public class GiftCardResponse {

    private Long userGiftCardId;
    private DurationType validForPlan;
    private String giftCardCode;
    private String discountPercent;
    private String description;
}
