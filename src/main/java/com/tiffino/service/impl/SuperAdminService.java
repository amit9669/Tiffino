package com.tiffino.service.impl;

import com.tiffino.config.AuthenticationService;
import com.tiffino.config.JwtService;
import com.tiffino.entity.*;
import com.tiffino.entity.request.*;
import com.tiffino.entity.response.*;
import com.tiffino.repository.*;
import com.tiffino.service.EmailService;
import com.tiffino.service.ISuperAdminService;
import com.tiffino.service.ImageUploadService;
import com.tiffino.service.OtpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SuperAdminService implements ISuperAdminService {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private CloudKitchenRepository kitchenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private OtpService otpService;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserOfferRepository userOfferRepository;

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private MealRepository mealRepository;

    /*@Value("${twilio.account.sid}")
    private String ACCOUNT_SID;

    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;

    @Value("${twilio.phone.number}")
    private String FROM_NUMBER;
*/
    @Autowired
    private EmailService emailService;

    private final Map<String, Integer> cityPrefixCounter = new HashMap<>();

    private final Map<String, Integer> cityDivisionCounter = new HashMap<>();

    @Override
    public Object updateAdmin(SuperAdminRequest superAdminRequest) {
        System.out.println(superAdminRequest.getEmail());
        if (superAdminRepository.existsByEmail(superAdminRequest.getEmail())) {
            SuperAdmin superAdmin = superAdminRepository.findByEmail(superAdminRequest.getEmail()).get();
            superAdmin.setAdminName(superAdminRequest.getAdminName());
            superAdmin.setEmail(superAdminRequest.getEmail());
            superAdmin.setPassword(passwordEncoder.encode(superAdminRequest.getPassword()));
            superAdminRepository.save(superAdmin);
            return "Updated Successfully!!!";
        } else {
            return "Email Invalid!!";
        }
    }


    @Override
    public Object saveCloudKitchen(CloudKitchenRequest kitchenRequest) {
        CloudKitchen cloudKitchen = new CloudKitchen();
        cloudKitchen.setCloudKitchenId(this.createCloudKitchenId(kitchenRequest.getCity(), kitchenRequest.getDivision()));
        cloudKitchen.setCity(kitchenRequest.getCity());
        cloudKitchen.setState(kitchenRequest.getState());
        cloudKitchen.setDivision(kitchenRequest.getDivision());
        kitchenRepository.save(cloudKitchen);
        return "Cloud Kitchen Inserted Successfully!!";
    }


    @Override
    public Object saveManager(ManagerRequest managerRequest) {
        CloudKitchen cloudKitchen = kitchenRepository.findById(managerRequest.getCloudKitchenId()).get();
        if (kitchenRepository.existsById(cloudKitchen.getCloudKitchenId())) {
            if(cloudKitchen.getManager()!=null){
                return "cloud kitchen has manager";
            }
            Manager manager = new Manager();
            manager.setManagerId(this.createManagerId(managerRequest.getCity()));
            manager.setManagerName(managerRequest.getManagerName());
            if (!emailService.isDeliverableEmail(managerRequest.getManagerEmail())) {
                return "Invalid or undeliverable email: " + managerRequest.getManagerEmail();
            }
            manager.setManagerEmail(managerRequest.getManagerEmail());
            manager.setCity(managerRequest.getCity());
            manager.setCurrentAddress(managerRequest.getCurrentAddress());
            manager.setDob(managerRequest.getDob());
            manager.setPermeantAddress(managerRequest.getPermeantAddress());
            manager.setPhoneNo(managerRequest.getPhoneNo());
            manager.setAdharCard(imageUploadService.uploadImage(managerRequest.getAdharCard()));
            manager.setPanCard(imageUploadService.uploadImage(managerRequest.getPanCard()));
            manager.setPhoto(imageUploadService.uploadImage(managerRequest.getPhoto()));
            manager.setCloudKitchen(cloudKitchen);
            Manager savedManager = managerRepository.save(manager);
//                this.sendSMS(manager.getPhoneNo());
            emailService.sendEmail(manager.getManagerEmail(), "Tiffino Manager Credential",
                    "Now You are the manager of " + cloudKitchen.getCloudKitchenId() + " this Cloud Kitchen and your Id is : "
                            + savedManager.getManagerId() + " and your One Time Password is : " + otpService.generateOTP(savedManager.getManagerEmail()));
            log.info("this is manager save api : {}", otpService.getOtp(savedManager.getManagerEmail()));

            String otpPassword = otpService.getOtp(savedManager.getManagerEmail()) + "";
            log.info("otpPassword :- " + otpPassword);
            savedManager.setPassword(passwordEncoder.encode(otpPassword));
            managerRepository.save(savedManager);
            return "Manager Inserted Successfully!!";
        } else {
            return "Invalid Cloud Kitchen Id";
        }
    }

    @Override
    public List<ManagerWithCKResponse> getAllManagersWithCloudKitchen() {
        List<CloudKitchen> cloudKitchens = kitchenRepository.findAllByIsDeletedFalse();
        List<ManagerWithCKResponse> managerWithCKResponses = new ArrayList<>();

        for (CloudKitchen cloudKitchen : cloudKitchens) {
            Manager manager = cloudKitchen.getManager(); // may be null

            ManagerWithCKResponse response = new ManagerWithCKResponse();

            if (manager != null) {
                response.setManagerId(manager.getManagerId());
            } else {
                response.setManagerId("Not Assigned");
            }

            response.setCloudKitchenId(cloudKitchen.getCloudKitchenId());
            response.setCloudKitchenDivision(cloudKitchen.getDivision());
            response.setCloudKitchenCity(cloudKitchen.getCity());
            response.setCloudKitchenState(cloudKitchen.getState());

            managerWithCKResponses.add(response);
        }

        return managerWithCKResponses;
    }


    public String createManagerId(String city) {
        if (city == null) {
            return "City Not Added!!";
        }
        String cityPrefix = city.substring(0, 3).toUpperCase();
        int currentCount = cityPrefixCounter.getOrDefault(cityPrefix, 0) + 1;

        cityPrefixCounter.put(cityPrefix, currentCount);
        String formattedNumber = String.format("%03d", currentCount);

        return "MAN" + cityPrefix + formattedNumber; // MANPUN001
    }


    public String createCloudKitchenId(String city, String division) {
        if (city == null || division == null || city.isBlank() || division.isBlank()) {
            return "City or Division not provided!";
        }
        String cityPrefix = city.trim().toUpperCase();
        cityPrefix = cityPrefix.length() >= 3 ? cityPrefix.substring(0, 3) : String.format("%-3s", cityPrefix).replace(' ', 'X');

        String divisionPrefix = division.trim().toUpperCase();
        divisionPrefix = divisionPrefix.length() >= 3 ? divisionPrefix.substring(0, 3) : String.format("%-3s", divisionPrefix).replace(' ', 'X');
        String cityDivision = cityPrefix + divisionPrefix;

        int count = cityDivisionCounter.getOrDefault(cityDivision, 0) + 1;
        cityDivisionCounter.put(cityDivision, count);
        String formattedCount = String.format("%03d", count);

        return cityPrefix + divisionPrefix + formattedCount;  // PUNKAT001 = Pune + Katraj + 001
    }


    /// /    SMS will get this message :- Sent from your Twilio trial account -Hello! Please check your Email Account!
    /// /    cause of free version has used
//    public void sendSMS(String phoneNo) {
//
//        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
//
//        Message message = Message.creator(
//                new PhoneNumber("+91" + phoneNo),    // To number
//                new PhoneNumber(FROM_NUMBER),  // From your Twilio number
//                "Hello! Please check your Email Account!"
//        ).create();
//
//        System.out.println("SMS sent with SID: " + message.getSid());
//    }
    @Override
    public Object deleteCloudKitchen(String kitchenId) {
        if (kitchenRepository.existsByCloudKitchenIdAndIsDeletedFalse(kitchenId)) {
            CloudKitchen cloudKitchen = kitchenRepository.findById(kitchenId).get();
            if (cloudKitchen.getIsActive()) {
                cloudKitchen.setIsDeleted(true);
                cloudKitchen.setIsActive(false);
                Manager manager = managerRepository.findByCloudKitchen_CloudKitchenId(cloudKitchen.getCloudKitchenId());
                manager.setCloudKitchen(null);
                managerRepository.save(manager);
                kitchenRepository.save(cloudKitchen);
                return "Deleted Successfully!!!";
            } else {
                return "Already Deleted";
            }
        } else {
            return "Id Not Found!";
        }
    }

    @Override
    public Object deleteManager(String managerId) {
        if (managerRepository.existsByManagerIdAndIsDeletedFalse(managerId)) {
            Manager manager = managerRepository.findById(managerId).get();
            if (manager.getIsActive()) {
                manager.setIsDeleted(true);
                manager.setIsActive(false);
                manager.setCloudKitchen(null);
                managerRepository.save(manager);
                return "Deleted Successfully!!!";
            } else {
                return "Already Deleted";
            }
        } else {
            return "Id Not Found!";
        }
    }

    @Override
    public Object searchFilterForAdmin(List<String> state, List<String> city, List<String> division) {

        if (state != null) {
            state = state.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (state.isEmpty()) state = null;
        }
        if (city != null) {
            city = city.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (city.isEmpty()) city = null;
        }
        if (division != null) {
            division = division.stream()
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (division.isEmpty()) division = null;
        }
        return managerRepository.getAllDetails(state, city, division);
    }

    @Override
    public Object saveOrUpdateSubscriptionPlan(SubscriptionRequest subscriptionRequest) {
        if (subscriptionRepository.existsById(subscriptionRequest.getId())) {
            Subscription subscription = subscriptionRepository.findById(subscriptionRequest.getId()).get();
            subscription.setPrice(subscriptionRequest.getPrice());
            subscription.setSubName(subscriptionRequest.getName());
            subscription.setDescription(subscriptionRequest.getDescription());
            subscription.setDurationType(subscriptionRequest.getDurationType());
            subscriptionRepository.save(subscription);
            return "updated Successfully";
        } else {
            Subscription subscription = new Subscription();
            subscription.setPrice(subscriptionRequest.getPrice());
            subscription.setSubName(subscriptionRequest.getName());
            subscription.setDescription(subscriptionRequest.getDescription());
            subscription.setDurationType(subscriptionRequest.getDurationType());
            subscriptionRepository.save(subscription);
            return "Inserted Successfully";
        }
    }

    @Override
    public Object getAllSubscription() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Object deleteSubscriptionPlan(Long subId) {
        subscriptionRepository.deleteById(subId);
        return "Subscription Plan has Deleted!!";
    }

    @Override
    public Object saveOrUpdateDeliveryPerson(DeliveryPersonRequest personRequest) {
        Optional<CloudKitchen> cloudKitchen = kitchenRepository.findByCloudKitchenIdAndIsDeletedFalse(personRequest.getCloudKitchenId());
        if (!cloudKitchen.isPresent()) {
            return "CloudKitchen Empty!!";
        }
        if (deliveryPersonRepository.existsById(personRequest.getDeliveryPersonId())) {
            DeliveryPerson deliveryPerson = deliveryPersonRepository.findById(personRequest.getDeliveryPersonId()).get();

            if (!emailService.isDeliverableEmail(personRequest.getEmail())) {
                return "Invalid or undeliverable email: " + personRequest.getEmail();
            }

            deliveryPerson.setEmail(personRequest.getEmail());
            deliveryPerson.setPhoneNo(personRequest.getPhoneNo());
            deliveryPerson.setName(personRequest.getName());
            deliveryPerson.setCloudKitchen(cloudKitchen.get());
            DeliveryPerson deliveryPersonSaved = deliveryPersonRepository.save(deliveryPerson);
            emailService.sendEmail(deliveryPerson.getEmail(), "Tiffino Delivery Partner Credential",
                    "Now You are the Delivery Partner of this " + deliveryPerson.getCloudKitchen().getCloudKitchenId()
                            + " cloudKitchen and your One Time Password is : " + otpService.generateOTP(deliveryPerson.getEmail()));
            deliveryPersonSaved.setPassword(passwordEncoder.encode(otpService.getOtp(deliveryPerson.getEmail()) + ""));
            deliveryPersonSaved.setRole(Role.DELIVERY_PERSON);
            deliveryPersonRepository.save(deliveryPersonSaved);
            return "Updated Successfully!!!";
        } else {
            DeliveryPerson deliveryPerson = new DeliveryPerson();

            if (!emailService.isDeliverableEmail(personRequest.getEmail())) {
                return "Invalid or undeliverable email: " + personRequest.getEmail();
            }

            deliveryPerson.setEmail(personRequest.getEmail());
            deliveryPerson.setPhoneNo(personRequest.getPhoneNo());
            deliveryPerson.setName(personRequest.getName());
            deliveryPerson.setCloudKitchen(cloudKitchen.get());
            DeliveryPerson deliveryPersonSaved = deliveryPersonRepository.save(deliveryPerson);
            emailService.sendEmail(deliveryPerson.getEmail(), "Tiffino Delivery Partner Credential",
                    "Now You are the Delivery Partner of this " + deliveryPerson.getCloudKitchen().getCloudKitchenId()
                            + " cloudKitchen and your One Time Password is : " + otpService.generateOTP(deliveryPerson.getEmail()));
            deliveryPersonSaved.setPassword(passwordEncoder.encode(otpService.getOtp(deliveryPerson.getEmail()) + ""));
            deliveryPersonSaved.setRole(Role.DELIVERY_PERSON);
            deliveryPersonRepository.save(deliveryPersonSaved);
            return "Inserted Successfully!!";
        }
    }

    @Override
    public String saveOrUpdateCuisine(CuisineRequest cuisineRequest) throws IOException {

        if (cuisineRequest.getCuisineId() != null && cuisineRepository.existsById(cuisineRequest.getCuisineId())) {
            Optional<Cuisine> cuisineOptional = cuisineRepository.findById(cuisineRequest.getCuisineId());

            if (!cuisineOptional.isPresent()) {
                return "Cuisine not found!!";
            }
            Cuisine cuisine = cuisineOptional.get();
            cuisine.setName(cuisineRequest.getName());
            cuisine.setDescription(cuisineRequest.getDescription());
            cuisine.setUpdatedAt(LocalDateTime.now());

            cuisineRepository.save(cuisine);
            return "Cuisine Updated Successfully!!";

        } else {

            Cuisine cuisine = new Cuisine();
            cuisine.setName(cuisineRequest.getName());
            cuisine.setDescription(cuisineRequest.getDescription());
            cuisine.setCreatedAt(LocalDateTime.now());
            cuisine.setUpdatedAt(LocalDateTime.now());

            cuisineRepository.save(cuisine);
            return "Cuisine Inserted Successfully!!";
        }
    }

    @Override
    public Object saveOrUpdateMeal(MealRequest mealRequest) {

        Optional<Cuisine> cuisine = cuisineRepository.findById(mealRequest.getCuisineId());
        if (!cuisine.isPresent()) {
            return "Id not found!!";
        }

        if (mealRequest.getMealId() != null && mealRepository.existsById(mealRequest.getMealId())) {
            Optional<Meal> mealOptional = mealRepository.findById(mealRequest.getMealId());
            if (!mealOptional.isPresent()) {
                return "Meal not found!!";
            }
            Meal meal = mealOptional.get();
            meal.setName(mealRequest.getName());
            meal.setDescription(mealRequest.getDescription());
            meal.setNutritionalInformation(mealRequest.getNutritionalInformation());
            meal.setPrice(mealRequest.getPrice());
            meal.setPhotos(this.imageUploadService.uploadImage(mealRequest.getPhotos()));
            meal.setCuisine(cuisine.get());
            meal.setUpdatedAt(LocalDateTime.now());

            mealRepository.save(meal);
            return "Meal Updated Successfully!!";

        } else {
            Meal meal = new Meal();
            meal.setName(mealRequest.getName());
            meal.setDescription(mealRequest.getDescription());
            meal.setNutritionalInformation(mealRequest.getNutritionalInformation());
            meal.setPrice(mealRequest.getPrice());
            meal.setPhotos(this.imageUploadService.uploadImage(mealRequest.getPhotos()));
            meal.setCuisine(cuisine.get());
            meal.setCreatedAt(LocalDateTime.now());
            meal.setUpdatedAt(LocalDateTime.now());

            mealRepository.save(meal);
            return "Meal Inserted Successfully!!";
        }

    }

    @Override
    public Object createOffer(OfferRequest request) {
        Offer offer = new Offer();
        offer.setType(request.getType());
        offer.setDescription(request.getDescription());
        offer.setTermsAndConditions(request.getTermsAndConditions());
        offer.setIsActive(request.getIsActive());
        offer.setTargetGroup(request.getTargetGroup());

        if (request.getSubscriptionId() != null) {
            Subscription plan = subscriptionRepository.findById(request.getSubscriptionId())
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));
            offer.setApplicablePlan(plan);
        }
        offerRepository.save(offer);
        return "Offer Created Successfully!!!";
    }

    @Override
    public Object getAllOffers() {
        return offerRepository.findAll();
    }

    @Override
    public Object assignOffersToEligibleUsers(Long offerId) {
        Optional<Offer> offerOptional = offerRepository.findById(offerId);

        if (!offerOptional.isPresent()) {
            return "Offer Not Found!!";
        }

        Offer offer = offerOptional.get();

        List<User> eligibleUsers = getEligibleUsers(offer);

        List<UserOffer> newUserOffers = new ArrayList<>();

        for (User user : eligibleUsers) {
            boolean alreadyAssigned = userOfferRepository
                    .existsByUser_UserIdAndOffer_OfferId(user.getUserId(), offerId);

            if (!alreadyAssigned) {
                UserOffer uo = new UserOffer();
                uo.setUser(user);
                uo.setOffer(offer);
                uo.setIsRedeemed(false);
                newUserOffers.add(uo);
            }
        }
        userOfferRepository.saveAll(newUserOffers);
        return "Assign Offers to eligible User";
    }

    private List<User> getEligibleUsers(Offer offer) {
        return switch (offer.getTargetGroup()) {
            case SUBSCRIBED -> userSubscriptionRepository.findActiveSubscribers();
            case UNSUBSCRIBED -> userRepository.findUsersWithoutActiveSubscription();
            case ALL -> userRepository.findAll();
        };
    }

    @Override
    public Object getAllOffersWithRedeemUsers() {
        List<UserOffer> userOfferList = userOfferRepository.findAll();

        return userOfferList.stream()
                .map(userOffer -> {
                    LocalDateTime redeemedAt = userOffer.getRedeemedAt();

                    String redeemedDate = redeemedAt != null ? redeemedAt.toLocalDate().toString() : null;
                    String redeemedTime = redeemedAt != null ? redeemedAt.toLocalTime().toString() : null;

                    return new UserRedeemOfferResponse(
                            userOffer.getUser().getUserName(),
                            userOffer.getOffer().getType(),
                            redeemedDate,
                            redeemedTime
                    );
                })
                .collect(Collectors.toList());
    }


    @Override
    public Object getAllSubscribedUser() {
        List<UserSubscription> allSubscribers = userSubscriptionRepository.findByIsSubscribedTrue();

        return allSubscribers.stream()
                .map(userSubscription -> AllUserSubscribers.builder()
                        .userName(userSubscription.getUser().getUserName())
                        .price(userSubscription.getPlan().getPrice())
                        .subscriptionName(userSubscription.getPlan().getSubName())
                        .durationType(userSubscription.getPlan().getDurationType())
                        .userEmail(userSubscription.getUser().getEmail())
                        .expiryDate(String.valueOf(userSubscription.getExpiryDate().toLocalDate()))
                        .expiryTime(String.valueOf(userSubscription.getExpiryDate().toLocalTime()))
                        .build()
                );
    }

    @Override
    public Object getAllCloudKItchenAndReviews() {
        List<CloudKitchen> cloudKitchenList = kitchenRepository.findAllByIsDeletedFalse();

        return cloudKitchenList.stream()
                .map(cloudKitchen -> {
                    List<ReviewResponse> reviewResponses = cloudKitchen.getReviews().stream()
                            .map(review -> new ReviewResponse(review.getComment(), review.getRating()))
                            .collect(Collectors.toList());

                    return DataOfCloudKitchenResponse.builder()
                            .cloudKitchenId(cloudKitchen.getCloudKitchenId())
                            .managerId(cloudKitchen.getManager().getManagerId())
                            .city(cloudKitchen.getCity())
                            .division(cloudKitchen.getDivision())
                            .reviews(reviewResponses)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Object getAllCuisines() {
        return cuisineRepository.findAll();
    }
}