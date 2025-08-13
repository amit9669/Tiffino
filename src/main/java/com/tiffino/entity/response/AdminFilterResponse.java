package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminFilterResponse {

    private String cloudKitchenId;
    private String state;
    private String city;
    private String division;
    private Boolean cloudKitchenIsActive;
    private Boolean cloudKitchenIsDeleted;
    private LocalDateTime cloudKitchenCreatedAt;
    private String managerId;
    private String managerName;
    private String managerEmail;
    private String dob;
    private String phoneNo;
    private String currentAddress;
    private String permeantAddress;
    private String adharCard;
    private String panCard;
    private String photo;
    private Boolean managerIsActive;
    private Boolean managerIsDeleted;
    private LocalDateTime managerCreatedAt;
}
