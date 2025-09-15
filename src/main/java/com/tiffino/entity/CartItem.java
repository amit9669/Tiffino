package com.tiffino.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cloud_kitchen_meal_id")
    private CloudKitchenMeal cloudKitchenMeal;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "price")
    private double price;
}

