package com.tiffino.entity.response;

import com.tiffino.entity.DeliveryStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryTrackingResponse {
    private Long orderId;
    private String orderStatus;
    private Long deliveryId;
    private DeliveryStatus deliveryStatus;
    private String deliveryPersonName;
    private String deliveryPersonPhone;
    private String allergies;
    private LocalDateTime assignedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
}
