package com.tiffino.entity.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MealRequest {

    private Long mealId;
    private String name;
    private String description;
    private String nutritionalInformation;
    private MultipartFile photos;
    private double price;
    private Long cuisineId;
}

