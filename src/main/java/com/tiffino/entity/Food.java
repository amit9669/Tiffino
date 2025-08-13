package com.tiffino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "food")
public class Food {

    @Id
    @Column(name = "food_id")
    private Long foodId;
    @Column(name = "food_name")
    private String foodName;

    @Column(name = "food_price")
    private Double foodPrice;

    @Column(name = "food_isAvailable")
    private Boolean isAvailable;

    @Column(name = "food_isUnavailable")
    private Boolean isUnavailable;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "cloud_kitchen_id", nullable = false)
    private CloudKitchen cloudKitchen;

}
