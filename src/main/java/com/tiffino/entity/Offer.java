package com.tiffino.entity;//package com.tiffino.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "offer_id")
    private Long offerId;

    @Column(nullable = false)
    private String type;  // e.g. "loyalty", "streak", "discount"

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "terms_and_conditions")
    private String termsAndConditions;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //user add manay to one mapping
}

//
//import jakarta.persistence.*;
//import lombok.*;
//import org.hibernate.annotations.CreationTimestamp;
//import org.hibernate.annotations.UpdateTimestamp;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "offers")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Offer {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long offerId;
//
//    private String type;
//    private String description;
//
//    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
//    private String termsAndConditions;
//
//    private Boolean isActive = true;
//
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
//}
