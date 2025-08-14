package com.tiffino.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

//    @ManyToOne(optional = false)
//    @JoinColumn(name = "meal_id")
//    private Meal meal;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must not exceed 5")
    private int rating;

    @Column(length = 1000)
    private String reviewText;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;




}
