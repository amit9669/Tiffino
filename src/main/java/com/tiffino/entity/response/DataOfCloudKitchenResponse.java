package com.tiffino.entity.response;

import com.tiffino.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataOfCloudKitchenResponse {

    private String cloudKitchenId;
    private String managerId;
    private String city;
    private String division;
    private String address;
    private Integer pinCode;
    private List<ReviewResponse> reviews;
}
