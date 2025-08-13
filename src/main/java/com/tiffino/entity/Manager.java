package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "manager")
public class Manager {

    @Id
    @Column(name = "manager_id")
    private String managerId;

    @Column(name = "manager_name")
    private String managerName;

    @Column(name = "manager_email")
    private String managerEmail;

    @Column(name = "dob")
    private String dob;

    @Column(name = "phone_no")
    private String phoneNo;

    @Column(name = "current_address")
    private String currentAddress;

    @Column(name = "permeant_address")
    private String permeantAddress;

    @Column(name = "adhar_card")
    private String adharCard;

    @Column(name = "pan_card")
    private String panCard;

    @Column(name = "photo")
    private String photo;

    @Column(name = "password")
    private String password;

    @Column(name = "city")
    private String city;

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

    @OneToOne
    @JoinColumn(name = "cloud_kitchen_id", referencedColumnName = "cloud_kitchen_id")
    @JsonManagedReference
    private CloudKitchen cloudKitchen;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.MANAGER;
}
