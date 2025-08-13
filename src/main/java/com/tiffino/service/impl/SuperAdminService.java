package com.tiffino.service.impl;

import com.tiffino.config.AuthenticationService;
import com.tiffino.config.JwtService;
import com.tiffino.entity.CloudKitchen;
import com.tiffino.entity.Manager;
import com.tiffino.entity.SuperAdmin;
import com.tiffino.entity.request.CloudKitchenRequest;
import com.tiffino.entity.request.ManagerRequest;
import com.tiffino.entity.request.SuperAdminRequest;
import com.tiffino.exception.CustomException;
import com.tiffino.repository.CloudKitchenRepository;
import com.tiffino.repository.ManagerRepository;
import com.tiffino.repository.SuperAdminRepository;
import com.tiffino.service.ISuperAdminService;
import com.tiffino.service.ImageUploadService;
import com.tiffino.service.OtpService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private AuthenticationService authenticationService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ImageUploadService imageUploadService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private OtpService otpService;

    /*@Value("${twilio.account.sid}")
    private String ACCOUNT_SID;

    @Value("${twilio.auth.token}")
    private String AUTH_TOKEN;

    @Value("${twilio.phone.number}")
    private String FROM_NUMBER;
*/
    private final Map<String, Integer> cityPrefixCounter = new HashMap<>();

    private final Map<String, Integer> cityDivisionCounter = new HashMap<>();


    @Override
    public Object saveOrUpdateAdmin(SuperAdminRequest superAdminRequest) {
        if (superAdminRepository.existsById(superAdminRequest.getSuperAdminId())) {
            SuperAdmin superAdmin = superAdminRepository.findById(superAdminRequest.getSuperAdminId()).get();
            superAdmin.setAdminName(superAdminRequest.getAdminName());
            superAdmin.setEmail(superAdminRequest.getEmail());
            superAdmin.setPassword(passwordEncoder.encode(superAdminRequest.getPassword()));
            superAdminRepository.save(superAdmin);
            return "Updated Successfully!!!";
        } else {
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setAdminName(superAdminRequest.getAdminName());
            superAdmin.setEmail(superAdminRequest.getEmail());
            superAdmin.setPassword(passwordEncoder.encode(superAdminRequest.getPassword()));
            superAdminRepository.save(superAdmin);
            return "Inserted Successfully!!!";
        }
    }


    @Override
    public Object saveOrUpdateCloudKitchen(CloudKitchenRequest kitchenRequest) {
        if (kitchenRepository.existsById(kitchenRequest.getCloudKitchenId())) {
            CloudKitchen cloudKitchen = kitchenRepository.findById(kitchenRequest.getCloudKitchenId()).get();
            cloudKitchen.setCloudKitchenId(kitchenRequest.getCloudKitchenId());
            cloudKitchen.setCity(kitchenRequest.getCity());
            cloudKitchen.setState(kitchenRequest.getState());
            cloudKitchen.setDivision(kitchenRequest.getDivision());
            kitchenRepository.save(cloudKitchen);
            return "Cloud Kitchen Updated Successfully!!";
        } else {
            CloudKitchen cloudKitchen = new CloudKitchen();
            cloudKitchen.setCloudKitchenId(this.createCloudKitchenId(kitchenRequest.getCity(), kitchenRequest.getDivision()));
            cloudKitchen.setCity(kitchenRequest.getCity());
            cloudKitchen.setState(kitchenRequest.getState());
            cloudKitchen.setDivision(kitchenRequest.getDivision());
            kitchenRepository.save(cloudKitchen);
            return "Cloud Kitchen Inserted Successfully!!";
        }
    }


    @Override
    public Object saveOrUpdateManager(ManagerRequest managerRequest) {
        CloudKitchen cloudKitchen = kitchenRepository.findById(managerRequest.getCloudKitchenId()).get();
        if (kitchenRepository.existsById(cloudKitchen.getCloudKitchenId())) {
            if (managerRepository.existsById(managerRequest.getManagerId())) {
                Manager manager = managerRepository.findById(managerRequest.getManagerId()).get();
                manager.setManagerName(managerRequest.getManagerName());
                manager.setManagerEmail(managerRequest.getManagerEmail());
                manager.setCity(managerRequest.getCity());
                manager.setCurrentAddress(managerRequest.getCurrentAddress());
                manager.setDob(managerRequest.getDob());
                manager.setPassword(passwordEncoder.encode(managerRequest.getPassword()));
                manager.setPermeantAddress(managerRequest.getPermeantAddress());
                manager.setPhoneNo(managerRequest.getPhoneNo());
                manager.setAdharCard(imageUploadService.uploadImage(managerRequest.getAdharCard()));
                manager.setPanCard(imageUploadService.uploadImage(managerRequest.getPanCard()));
                manager.setPhoto(imageUploadService.uploadImage(managerRequest.getPhoto()));
                manager.setCloudKitchen(cloudKitchen);
                managerRepository.save(manager);
//                this.sendSMS(manager.getPhoneNo());
                this.sendEmail(manager.getManagerEmail(), "Tiffino Manager Credential",
                        "Now You are the manager of" + cloudKitchen.getCloudKitchenId() + " this Cloud Kitchen and your Id is : "
                                + manager.getManagerId() + " and your One Time Password is : " + otpService.generateOTP(manager.getManagerEmail()));
                return "Manager Updated Successfully!!";

            } else {
                Manager manager = new Manager();
                manager.setManagerId(this.createManagerId(managerRequest.getCity()));
                manager.setManagerName(managerRequest.getManagerName());
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
                this.sendEmail(manager.getManagerEmail(), "Tiffino Manager Credential",
                        "Now You are the manager of " + cloudKitchen.getCloudKitchenId() + " this Cloud Kitchen and your Id is : "
                                + savedManager.getManagerId() + " and your One Time Password is : " + otpService.generateOTP(savedManager.getManagerEmail()));
                log.info("this is manager save api : {}", otpService.getOtp(savedManager.getManagerEmail()));

                String otpPassword = otpService.getOtp(savedManager.getManagerEmail()) + "";
                log.info("otpPassword :- "+otpPassword);
                savedManager.setPassword(passwordEncoder.encode(otpPassword));
                managerRepository.save(savedManager);
                return "Manager Inserted Successfully!!";
            }
        } else {
            return "Invalid Cloud Kitchen Id";
        }
    }


    public String createManagerId(String city) {
        if (city == null) {
            throw new CustomException("City Not Added!!");
        }
        String cityPrefix = city.substring(0, 3).toUpperCase();
        int currentCount = cityPrefixCounter.getOrDefault(cityPrefix, 0) + 1;

        cityPrefixCounter.put(cityPrefix, currentCount);
        String formattedNumber = String.format("%03d", currentCount);

        return "MAN" + cityPrefix + formattedNumber; // MANPUN001
    }


    public String createCloudKitchenId(String city, String division) {
        if (city == null || division == null || city.isBlank() || division.isBlank()) {
            throw new CustomException("City or Division not provided!");
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


////    SMS will get this message :- Sent from your Twilio trial account -Hello! Please check your Email Account!
////    cause of free version has used
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
        if (kitchenRepository.existsById(kitchenId)) {
            CloudKitchen cloudKitchen = kitchenRepository.findById(kitchenId).get();
            if (cloudKitchen.getIsActive()) {
                cloudKitchen.setIsDeleted(true);
                cloudKitchen.setIsActive(false);
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
        if (managerRepository.existsById(managerId)) {
            Manager manager = managerRepository.findById(managerId).get();
            if (manager.getIsActive()) {
                manager.setIsDeleted(true);
                manager.setIsActive(false);
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
}