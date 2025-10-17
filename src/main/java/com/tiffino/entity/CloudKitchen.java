package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "cloud_kitchen")
public class CloudKitchen {

    @Id
    @Column(name = "cloud_kitchen_id")
    private String cloudKitchenId;

    @Column(name = "state")
    private String state;

    @Column(name = "city")
    private String city;

    @Column(name = "division")
    private String division;

    @Column(name = "address")
    private String address;

    @Column(name = "pin_code")
    private Integer pinCode;

    @Column(name = "isActive")
    private Boolean isActive = true;

    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "cloudKitchen")
    @JsonBackReference
    private Manager manager;

    @Column(name = "is_Opened")
    private Boolean isOpened = true ;

    @OneToMany(mappedBy = "cloudKitchen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "cloudKitchen")
    @JsonManagedReference
    private List<DeliveryPerson> deliveryPersons;
}
