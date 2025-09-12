package com.tiffino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_gift_card")
public class UserGiftCards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userGiftCardId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_card_id", nullable = false)
    private GiftCards giftCards;

    @Enumerated(EnumType.STRING)
    @Column(name = "valid_for_plan", nullable = false)
    private DurationType validForPlan;

    @Column(name = "gift_card_code", unique = true, nullable = false)
    private String giftCardCode;

    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent;   // changes each cycle

    @Column(name = "is_redeemed")
    private Boolean isRedeemed = false;

    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
