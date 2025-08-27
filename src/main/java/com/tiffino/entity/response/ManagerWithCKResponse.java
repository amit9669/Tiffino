package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerWithCKResponse {

    private String managerId;
    private String cloudKitchenId;
    private String cloudKitchenState;
    private String cloudKitchenCity;
    private String cloudKitchenDivision;
}
