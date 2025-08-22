package com.tiffino.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "meals")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Meal {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "nutritional_information")
    private String nutritionalInformation;

    @Column(name = "photos")
    private String photos;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_available")
    private boolean available = true;

    @Column(name = "is_unavailable")
    private boolean isUnavailable = false;

    @Column(name = "price")
    private double price;

    @ManyToOne
    @JoinColumn(name = "cuisine_id", nullable = false)
    private Cuisine cuisine;
}
