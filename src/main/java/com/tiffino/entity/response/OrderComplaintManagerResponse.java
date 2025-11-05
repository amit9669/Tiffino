package com.tiffino.entity.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderComplaintManagerResponse {

    private Long complaintId;
    private String customerName;
    private String customerAddress;
    private String customerPhoneNo;
    private Long orderId;
    private String imageUrl;
    private String complaint;
}
