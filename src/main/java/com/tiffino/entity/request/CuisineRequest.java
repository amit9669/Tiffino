package com.tiffino.entity.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CuisineRequest {

    private Long cuisineId;
    private String name;
    private String description;
    private MultipartFile image;
}
