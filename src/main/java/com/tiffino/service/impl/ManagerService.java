package com.tiffino.service.impl;

import com.tiffino.config.AuthenticationService;
import com.tiffino.config.JwtService;
import com.tiffino.entity.*;
import com.tiffino.entity.response.*;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.*;
import com.tiffino.service.DataToken;
import com.tiffino.service.IManagerService;
import com.tiffino.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<CuisineWithMealsResponse> getAllCuisinesAndMeals() {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();
        CloudKitchen cloudKitchen = manager.getCloudKitchen();

        return cuisineRepository.findAll().stream()
                .map(cuisine -> new CuisineWithMealsResponse(
                        cuisine.getName(),
                        cuisine.getMeals().stream()
                                .map(meal -> {
                                    boolean available = cloudKitchenMealRepository
                                            .findByCloudKitchenAndMeal(cloudKitchen, meal)
                                            .map(CloudKitchenMeal::isAvailable)
                                            .orElse(false);

                                    MealSummaryResponse msr = new MealSummaryResponse(meal.getMealId(), meal.getName(), available);
                                    return msr;
                                })
                                .toList()
                ))
                .toList();
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
        CloudKitchen cloudKitchen = kitchenRepository.findByCloudKitchenIdAndIsDeletedFalse(manager.getCloudKitchen().getCloudKitchenId()).get();

        List<ReviewResponse> reviewResponses = cloudKitchen.getReviews().stream()
                .map(review -> new ReviewResponse(review.getCloudKitchenReview(), review.getRating()))
                .collect(Collectors.toList());

        return DataOfCloudKitchenResponse.builder()
                .cloudKitchenId(cloudKitchen.getCloudKitchenId())
                .division(cloudKitchen.getDivision())
                .address(cloudKitchen.getAddress())
                .state(cloudKitchen.getState())
                .pinCode(cloudKitchen.getPinCode())
                .city(cloudKitchen.getCity())
                .managerId(manager.getManagerId())
                .reviews(reviewResponses)//-------------------------remaining
                .build();
    }

    @Override
    public Object enableMealForKitchen(Long mealId) {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = manager.getCloudKitchen();

        Optional<Meal> mealOptional = mealRepository.findById(mealId);

        if (!mealOptional.isPresent()) {
            return "Meal not found: " + mealId;
        }

        Meal meal = mealOptional.get();

        Optional<CloudKitchenMeal> existing = cloudKitchenMealRepository.findByCloudKitchenAndMeal(cloudKitchen, meal);
        CloudKitchenMeal cloudKitchenMeal = existing.orElse(new CloudKitchenMeal());

        cloudKitchenMeal.setCloudKitchen(cloudKitchen);
        cloudKitchenMeal.setMeal(meal);
        cloudKitchenMeal.setAvailable(true);
        cloudKitchenMeal.setUnavailable(false);
        cloudKitchenMealRepository.save(cloudKitchenMeal);
        return "Add Meals " + mealId;
    }

    @Override
    public Object getAllCloudKitchenMealIsAvailable() {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();
        List<CloudKitchenMeal> aTrue = cloudKitchenMealRepository.findByCloudKitchenAndAvailableTrue(manager.getCloudKitchen());
        List<Meal> meals = new ArrayList<>();
        for (CloudKitchenMeal meal : aTrue) {
            Meal meal1 = new Meal();
            meal1.setMealId(meal.getMeal().getMealId());
            meal1.setName(meal.getMeal().getName());
            meal1.setPhotos(meal.getMeal().getPhotos());
            meal1.setPrice(meal.getMeal().getPrice());
            meal1.setDescription(meal.getMeal().getDescription());
            meals.add(meal1);
        }
        return meals;
    }

    @Override
    public Object disableMealForKitchen(Long mealId) {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = manager.getCloudKitchen();

        Optional<Meal> mealOptional = mealRepository.findById(mealId);

        if (!mealOptional.isPresent()) {
            return "Meal not found: " + mealId;
        }

        Meal meal = mealOptional.get();

        CloudKitchenMeal cloudKitchenMeal = cloudKitchenMealRepository
                .findByCloudKitchenAndMeal(cloudKitchen, meal)
                .orElseThrow(() -> new RuntimeException("Meal not assigned to this kitchen: " + mealId));

        cloudKitchenMeal.setAvailable(false);
        cloudKitchenMeal.setUnavailable(true);
        cloudKitchenMealRepository.save(cloudKitchenMeal);
        return "Disable Meal for Cloud-Kitchen" + mealId;
    }

    @Override
    public Object assignOrderToDeliveryPerson(Long orderId, Long deliveryPersonId) {

        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getOrderStatus().equals("PENDING") && !order.getOrderStatus().equals("CONFIRMED")) {
            return "Order already assigned or processed";
        }

        DeliveryPerson dp = deliveryPersonRepository.findById(deliveryPersonId)
                .orElseThrow(() -> new RuntimeException("Delivery person not found"));

        CloudKitchen managerCK = managerRepository.findById(manager.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"))
                .getCloudKitchen();

        if (!dp.getCloudKitchen().getCloudKitchenId().equals(managerCK.getCloudKitchenId())) {
            return "You can only assign delivery persons of your CloudKitchen";
        }

        if (!dp.getIsAvailable()) {
            return "Delivery person not available";
        }

        dp.setIsAvailable(false);
        DeliveryPerson savedDeliveryPerson = deliveryPersonRepository.save(dp);

        Delivery delivery = Delivery.builder()
                .order(order)
                .deliveryPerson(dp)
                .status(DeliveryStatus.ASSIGNED)
                .assignedAt(LocalDateTime.now())
                .build();

        order.setOrderStatus("ASSIGNED_TO_DELIVERY");
        orderRepository.save(order);

        deliveryRepository.save(delivery);

        this.sendEmail(savedDeliveryPerson.getEmail(), "Assign Order by Manager " + manager.getManagerId(),
                "You have assigned order and Order Id is " + orderId);

        return "assign an Order To DeliveryPerson, Name is " + dp.getName();
    }

    @Override
    public Object getAllOrders() {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        List<Order> orders = orderRepository.findAllByIsAvailableTrue().stream()
                .filter(order -> manager.getCloudKitchen().getCloudKitchenId()
                        .equals(order.getCloudKitchen().getCloudKitchenId()))
                .toList();

        return orders.stream()
                .map(order -> OrderResponseForManager.builder()
                        .orderId(order.getOrderId())
                        .orderStatus(order.getOrderStatus())
                        .totalCost(order.getTotalCost())
                        .allergies(order.getDeliveryDetails().getAllergies())
                        .address(order.getDeliveryDetails().getAddress())
                        .orderDate(String.valueOf(order.getCreatedAt().toLocalDate()))
                        .orderTime(String.valueOf(order.getCreatedAt().toLocalTime().truncatedTo(ChronoUnit.SECONDS)))
                        .userName(order.getUser().getUserName())
                        .build()
                )
                .toList();
    }

    @Override
    public Object listOfDeliveryPersonIsAvailable() {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();
        List<DeliveryPerson> deliveryPeople = deliveryPersonRepository.findByIsAvailableTrue();

        return deliveryPeople.stream()
                .filter(dp -> dp.getCloudKitchen().getCloudKitchenId()
                        .equals(manager.getCloudKitchen().getCloudKitchenId())).toList();
    }

    @Override
    public Object addOrRemoveMeals(Long mealId) {
        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        CloudKitchen cloudKitchen = manager.getCloudKitchen();

        if (cloudKitchen.getIsDeleted()) {
            return "Cloud Kitchen is deleted!";
        }

        Optional<Meal> mealOptional = mealRepository.findById(mealId);

        if (!mealOptional.isPresent()) {
            return "Meal not found: " + mealId;
        }

        Meal meal = mealOptional.get();

        Optional<CloudKitchenMeal> existing = cloudKitchenMealRepository.findByCloudKitchenAndMeal(cloudKitchen, meal);
        CloudKitchenMeal cloudKitchenMeal = existing.orElse(null);

        if (cloudKitchenMeal == null) {
            cloudKitchenMeal = new CloudKitchenMeal();
            cloudKitchenMeal.setCloudKitchen(cloudKitchen);
            cloudKitchenMeal.setMeal(meal);
            cloudKitchenMeal.setAvailable(true);
            cloudKitchenMeal.setUnavailable(false);
            cloudKitchenMealRepository.save(cloudKitchenMeal);
            return "Enable Meal for Cloud-Kitchen " + mealId;
        } else {
            if (!cloudKitchenMeal.isAvailable()) {
                cloudKitchenMeal.setAvailable(true);
                cloudKitchenMeal.setUnavailable(false);
                cloudKitchenMealRepository.save(cloudKitchenMeal);
                return "Enable Meal for Cloud-Kitchen " + mealId;
            } else {
                cloudKitchenMeal.setAvailable(false);
                cloudKitchenMeal.setUnavailable(true);
                cloudKitchenMealRepository.save(cloudKitchenMeal);
                return "Disable Meal for Cloud-Kitchen " + mealId;
            }
        }
    }

    @Override
    public Object acceptedOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).get();

        if(order.getOrderStatus().equalsIgnoreCase("PENDING")){
            order.setOrderStatus("ACCEPTED-ORDER");
        }
        orderRepository.save(order);
        return "ACCEPTED-ORDER";
    }

    @Override
    public Object orderPrepared(Long orderId) {
        Order order = orderRepository.findById(orderId).get();

        if(order.getOrderStatus().equalsIgnoreCase("ACCEPTED-ORDER")){
            order.setOrderStatus("ORDER-PREPARED");
        }
        orderRepository.save(order);
        return "ORDER-PREPARED";
    }

    @Override
    public Object getAllDetails() {

        Manager manager = (Manager) dataToken.getCurrentUserProfile();

        List<Order> orders = orderRepository.findAllByIsAvailableTrue().stream()
                .filter(order -> manager.getCloudKitchen().getCloudKitchenId()
                        .equals(order.getCloudKitchen().getCloudKitchenId()))
                .toList();



        return null;
    }
}
