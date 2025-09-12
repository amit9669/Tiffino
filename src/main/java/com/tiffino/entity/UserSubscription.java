package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_subscription")
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_sub_id")
    private Long userSubId;

    @Column(name = "subscription_plan")
    @Enumerated(EnumType.STRING)
    private DurationType durationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonBackReference
    private User user;

    @Column(name = "is_subscribed")
    private Boolean isSubscribed;

    @Column(name = "start_date")
    private LocalDateTime startDate = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @ElementCollection
    @CollectionTable(name = "user_sub_mealtimes", joinColumns = @JoinColumn(name = "user_sub_id"))
    @Column(name = "meal_time")
    private Set<String> mealTimes = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "user_sub_allergies", joinColumns = @JoinColumn(name = "user_sub_id"))
    @Column(name = "allergy")
    private Set<String> allergies = new HashSet<>();

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "dietary_file")
    private String dietaryFilePath;
}
