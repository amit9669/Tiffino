package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminFilterResponse {

    private String cloudKitchenId;
    private String city;
    private String division;
    private Boolean cloudKitchenIsActive;
    private Boolean cloudKitchenIsDeleted;
    private LocalDateTime cloudKitchenCreatedAt;
    private String managerId;
    private String managerName;
    private Boolean managerIsActive;
    private Boolean managerIsDeleted;
    private LocalDateTime managerCreatedAt;
}
