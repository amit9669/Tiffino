package com.tiffino.entity.response;

import com.tiffino.entity.DurationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class AllUserSubscribers {

    private String userName;
    private String userEmail;
    private String subscriptionName;
    private Double price;
    private Set<String> mealsTime;
    private String expiryDate;
    private String expiryTime;
}
