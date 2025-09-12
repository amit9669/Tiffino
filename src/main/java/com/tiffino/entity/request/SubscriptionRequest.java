package com.tiffino.entity.request;

import com.tiffino.entity.DurationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionRequest {

    @NotNull(message = "Duration type is required")
    private DurationType durationType;
    @NotEmpty(message = "At least one meal time must be selected")
    private Set<String> mealTimes;
    private Set<String> allergies;
    private Integer caloriesPerMeal;
    private MultipartFile dietaryFilePath;
    private String giftCardCodeInput;
}
