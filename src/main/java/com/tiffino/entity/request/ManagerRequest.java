package com.tiffino.entity.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManagerRequest {

    private String managerId;
    private String managerName;
    private String managerEmail;
    private String dob;
    private String phoneNo;
    private String currentAddress;
    private String permeantAddress;
    private MultipartFile adharCard;
    private MultipartFile panCard;
    private MultipartFile photo;
    private String password;
    private String city;
    private String cloudKitchenId;
}
