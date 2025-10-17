package com.tiffino.entity.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudKitchenInfo {
    private String cloudKitchenId;
    private String cloudKitchenName;
    private Boolean isOpened;
}