package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "delivery_person")
public class DeliveryPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryPersonId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    private String phoneNo;

    @Column(name = "isAvailable")
    private Boolean isAvailable = true;

    @Column(name = "isActive")
    private Boolean isActive = true;

    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.DELIVERY_PERSON;

    @Column(name = "licences")
    private String licences;

    @Column(name = "adhar_card")
    private String adharCard;

    @Column(name = "insurance")
    private String insurance;

    @OneToMany(mappedBy = "deliveryPerson")
    @JsonIgnore
    private List<Delivery> deliveries;

    @ManyToOne
    @JoinColumn(name = "cloud_kitchen_id")
    @JsonBackReference
    private CloudKitchen cloudKitchen;
}
