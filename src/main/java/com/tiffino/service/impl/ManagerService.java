package com.tiffino.service.impl;

import com.tiffino.config.AuthenticationService;
import com.tiffino.config.JwtService;
import com.tiffino.entity.*;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.*;
import com.tiffino.service.DataToken;
import com.tiffino.service.IManagerService;
import com.tiffino.service.OtpService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ManagerService implements IManagerService {

    @Autowired
    private OtpService otpService;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private DataToken dataToken;

    @Autowired
    private CloudKitchenRepository kitchenRepository;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private CloudKitchenMealRepository cloudKitchenMealRepository;

    @Autowired
    private MealRepository mealRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;


    @Override
    public Object updatePassword(String managerId, int otp, String newPassword) {
        if (managerRepository.existsById(managerId)) {
            Manager manager = managerRepository.findById(managerId).get();
            if (otpService.getOtp(manager.getManagerEmail()) == otp) {
                otpService.clearOTP(manager.getManagerEmail());
                System.out.println("New Password : " + newPassword);
                manager.setPassword(passwordEncoder.encode(newPassword));
                managerRepository.save(manager);
                return "Password Updated Successfully!!";
            } else {
                return "OTP NOT MATCHED!!";
            }
        } else {
            return "Incorrect Id!!";
        }
    }

    @Override
    public Object forgotPasswordOfManager(String email, HttpSession session) {
        System.out.println(email);
        log.info("Email :-->" + email);
        if (managerRepository.existsByManagerEmail(email)) {
            this.sendEmail(email, "For Update Password", "This is your OTP :- " + otpService.generateOTP(email));
            session.setAttribute("email", email);
            return "Check email for OTP verification!";
        } else {
            return "This Email not exists!! First Create account!!";
        }
    }

    @Override
    public Object changePassword(int otp, String newPassword, String confirmNewPassword, HttpSession session) {
        if (otpService.getOtp((String) session.getAttribute("email")) == otp) {
            Manager manager = managerRepository.findByManagerEmail((String) session.getAttribute("email")).get();
            if (newPassword.equals(confirmNewPassword)) {
                manager.setPassword(passwordEncoder.encode(newPassword));
                managerRepository.save(manager);
                otpService.clearOTP(manager.getManagerEmail());
                return "Password has changed!!";
            } else {
                return "password doesn't match!! Please Try Again!!";
            }
        } else {
            return "OTP not Matched!!!";
        }
    }

    public void sendEmail(String to, String subject, String message) {
        try {
            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(message);
            javaMailSender.send(email);
        } catch (CustomException e) {
            log.error("Exception while send Email ", e);
        }
    }

    @Override
    public Object getDataOfCloudKitchen() {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();
        System.out.println(manager);
        return kitchenRepository.findById(manager.getCloudKitchen().getCloudKitchenId()).get();
    }

    @Override
    public Object enableMealForKitchen(List<Long> mealIds) {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = manager.getCloudKitchen();

        List<CloudKitchenMeal> results = new ArrayList<>();

        for (Long mealId : mealIds) {
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new RuntimeException("Meal not found: " + mealId));

            Optional<CloudKitchenMeal> existing = cloudKitchenMealRepository.findByCloudKitchenAndMeal(cloudKitchen, meal);
            CloudKitchenMeal cloudKitchenMeal = existing.orElse(new CloudKitchenMeal());

            cloudKitchenMeal.setCloudKitchen(cloudKitchen);
            cloudKitchenMeal.setMeal(meal);
            cloudKitchenMeal.setAvailable(true);
            cloudKitchenMeal.setUnavailable(false);
            results.add(cloudKitchenMealRepository.save(cloudKitchenMeal));
        }
        return results;
    }

    @Override
    public void disableMealForKitchen(List<Long> mealIds) {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = manager.getCloudKitchen();

        for (Long mealId : mealIds) {
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new RuntimeException("Meal not found: " + mealId));

            CloudKitchenMeal cloudKitchenMeal = cloudKitchenMealRepository
                    .findByCloudKitchenAndMeal(cloudKitchen, meal)
                    .orElseThrow(() -> new RuntimeException("Meal not assigned to this kitchen: " + mealId));

            cloudKitchenMeal.setAvailable(false);
            cloudKitchenMeal.setUnavailable(true);
            cloudKitchenMealRepository.save(cloudKitchenMeal);
        }
    }

    @Override
    public Object assignOrderToDeliveryPerson(Long orderId, Long deliveryPersonId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getOrderStatus().equals("PENDING") && !order.getOrderStatus().equals("CONFIRMED")) {
            throw new RuntimeException("Order already assigned or processed");
        }

        DeliveryPerson dp = deliveryPersonRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new RuntimeException("Delivery person not found"));

        if (!dp.getIsAvailable()) {
            throw new RuntimeException("Delivery person not available");
        }

        dp.setIsAvailable(false);
        deliveryPersonRepository.save(dp);

        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryPerson(dp)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        order.setOrderStatus("ASSIGNED_TO_DELIVERY");
        orderRepository.save(order);

        deliveryRepository.save(delivery);

        return "assign an Order To DeliveryPerson, Name is " + dp.getName();
    }
}
