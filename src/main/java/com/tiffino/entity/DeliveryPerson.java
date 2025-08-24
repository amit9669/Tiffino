package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    private String phoneNo;

    @Column(name = "isAvailable")
    private Boolean isAvailable = true;

    @Column(name = "isActive")
    private Boolean isActive = true;

    @Column(name = "isDeleted")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "deliveryPerson")
    @JsonIgnore
    private List<Delivery> deliveries;
}
