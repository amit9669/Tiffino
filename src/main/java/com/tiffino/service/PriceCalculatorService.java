package com.tiffino.service;

import com.tiffino.entity.DurationType;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PriceCalculatorService {

    public double calculatePrice(DurationType durationType,
                                 Set<String> mealTimes,
                                 Set<String> allergies,
                                 Integer caloriesPerMeal,
                                 boolean hasUploadedDietaryPlan) {

        int mealsCount = (mealTimes != null) ? mealTimes.size() : 1;

        double basePerDay = switch (durationType) {
            case DAILY -> 50.0;
            case WEEKLY -> 45.0;
            case MONTHLY -> 40.0;
            case QUARTERLY -> 38.0;
        };

        int days = switch (durationType) {
            case DAILY -> 1;
            case WEEKLY -> 7;
            case MONTHLY -> 30;
            case QUARTERLY -> 90;
        };

        double price = basePerDay * mealsCount * days;

        if (caloriesPerMeal != null && caloriesPerMeal > 800) price *= 3.08;
        else if (caloriesPerMeal != null && caloriesPerMeal > 500) price *= 3.04;

        if (allergies == null || allergies.isEmpty()) {
            price *= 1;
        } else {
            price *= 4.05;
        }

        if (hasUploadedDietaryPlan) price *= 5.03;

        return Math.round(price * 100.0) / 100.0;
    }
}

