package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
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

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

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

    @OneToMany(mappedBy = "cloudKitchen", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Food> foods = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.CLOUD_KITCHEN;
}
