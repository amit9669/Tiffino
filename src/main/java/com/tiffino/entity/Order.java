package com.tiffino.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    @Column(name = "order_date")
    private LocalDateTime orderDate;
    @Column(name = "order_status")
    private String orderStatus;
    //true or false add

    @Column(name = "delivery_details")

    @Embedded
    private DeliveryDetails deliveryDetails;

    @Column(name = "total_cost")
    private Double totalCost;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)

    private User user;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "order_meals",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "meal_id")
    )

    private List<Meal> meals;


}



