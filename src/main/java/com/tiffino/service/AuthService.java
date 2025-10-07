package com.tiffino.service;

import com.tiffino.config.JwtService;
import com.tiffino.entity.*;
import com.tiffino.entity.response.AuthResponse;
import com.tiffino.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private SuperAdminRepository superAdminRepo;

    @Autowired
    private ManagerRepository managerRepo;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DataToken dataToken;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private DeliveryPersonRepository deliveryPersonRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;


    public AuthResponse login(String emailOrId, String password) {

        Optional<User> user = userRepository.findByEmail(emailOrId);
        if (user.isPresent()) {
            if (passwordEncoder.matches(password, user.get().getPassword())) {
                return new AuthResponse(jwtService.generateToken(user.get().getEmail(), Role.USER.name()),
                        Role.USER.name(),
                        "Login successful");
            }
            return new AuthResponse(null, null, "Invalid User credentials");
        }

        Optional<SuperAdmin> superAdmin = superAdminRepo.findByEmail(emailOrId);
        if (superAdmin.isPresent()) {
            if (passwordEncoder.matches(password, superAdmin.get().getPassword())) {
                return new AuthResponse(jwtService.generateToken(superAdmin.get().getEmail(), Role.SUPER_ADMIN.name()),
                        Role.SUPER_ADMIN.name(),
                        "Login successful");
            }
            return new AuthResponse(null, null, "Invalid SuperAdmin credentials");
        }

        Optional<Manager> manager = managerRepo.findByManagerEmail(emailOrId);
        if (manager.isPresent()) {
            if (passwordEncoder.matches(password, manager.get().getPassword())) {
                return new AuthResponse(jwtService.generateToken(manager.get().getManagerEmail(), Role.MANAGER.name()),
                        Role.MANAGER.name(),
                        "Login successful");
            }
            return new AuthResponse(null, null, "Invalid Manager credentials");
        }

        Optional<Manager> managerById = managerRepo.findById(emailOrId);
        if (managerById.isPresent()) {
            if (passwordEncoder.matches(password, managerById.get().getPassword())) {
                return new AuthResponse(jwtService.generateToken(managerById.get().getManagerEmail(), Role.MANAGER.name()),
                        Role.MANAGER.name(),
                        "Login successful");
            }
            return new AuthResponse(null, null, "Invalid Manager ID credentials");
        }

        Optional<DeliveryPerson> deliveryPerson = deliveryPersonRepository.findByEmail(emailOrId);
        if (deliveryPerson.isPresent()) {
            if (passwordEncoder.matches(password, deliveryPerson.get().getPassword())) {
                return new AuthResponse(jwtService.generateToken(deliveryPerson.get().getEmail(), Role.DELIVERY_PERSON.name()),
                        Role.DELIVERY_PERSON.name(), "LogIn Successfully!");
            }
        }

        return new AuthResponse(null, null, "Invalid credentials");
    }


    public Object getProfile() {
        Object userProfile = dataToken.getCurrentUserProfile();

        if (userProfile instanceof User) {
            User user = (User) userProfile;

            UserSubscription userSubscription = userSubscriptionRepository
                    .findByIsSubscribedTrueAndUser_UserId(user.getUserId());

            if (userSubscription != null) {
                user.setDurationType(userSubscription.getDurationType());
                user.setEndTime(userSubscription.getExpiryDate().toLocalTime().truncatedTo(ChronoUnit.SECONDS));
                user.setEndDate(userSubscription.getExpiryDate().toLocalDate());
            }
            return user;
        }
        return userProfile;
    }


    public Object forgotPassword(String emailOrId, HttpSession session) {
        Optional<User> user = userRepository.findByEmail(emailOrId);
        if (user.isPresent()) {
            System.out.println("user :- " + user.get().getEmail());
            session.setAttribute("emailOrId", emailOrId);
            this.emailService.sendEmail(user.get().getEmail(), "OTP for Change Password", "Your OTP is :-" + this.otpService.generateOTP(emailOrId));
            return "Check your Email!!!";
        }

        Optional<Manager> manager = managerRepo.findByManagerEmail(emailOrId);
        if (manager.isPresent()) {
            System.out.println("manager :- " + manager.get().getManagerEmail());
            session.setAttribute("emailOrId", emailOrId);
            this.emailService.sendEmail(manager.get().getManagerEmail(), "OTP for Change Password", "Your OTP is :-" + this.otpService.generateOTP(emailOrId));
            return "Check your Email!!!";
        }

        Optional<Manager> managerById = managerRepo.findById(emailOrId);
        if (managerById.isPresent()) {
            System.out.println("managerId :- " + managerById.get().getManagerEmail());
            session.setAttribute("emailOrId", emailOrId);
            this.emailService.sendEmail(managerById.get().getManagerEmail(), "OTP for Change Password", "Your OTP is :-" + this.otpService.generateOTP(emailOrId));
            return "Check your Email!!!";
        }

        Optional<DeliveryPerson> deliveryPerson = deliveryPersonRepository.findByEmail(emailOrId);
        if (deliveryPerson.isPresent()) {
            session.setAttribute("emailOrId", emailOrId);
            this.emailService.sendEmail(deliveryPerson.get().getEmail(), "OTP for Change Password", "Your OTP is :-" + this.otpService.generateOTP(emailOrId));
            return "Check your Email!!!";
        }
        return new AuthResponse("Invalid User credentials");
    }


    public Object changePassword(int otp, String newPassword, String confirmNewPassword, HttpSession session) {

        String emailOrId = (String) session.getAttribute("emailOrId");
        System.out.println("changePassword----->"+emailOrId);

        Optional<User> user = userRepository.findByEmail(emailOrId);
        if (user.isPresent()) {
            if (newPassword.equals(confirmNewPassword) && otp == this.otpService.getOtp(emailOrId)) {
                user.get().setPassword(passwordEncoder.encode(confirmNewPassword));
                userRepository.save(user.get());
                return "Password has Changed!!!";
            } else {
                return "Invalid Credentials";
            }
        }
        

        Optional<Manager> manager = managerRepo.findByManagerEmail(emailOrId);
        if (manager.isPresent()) {
            if (newPassword.equals(confirmNewPassword) && otp == this.otpService.getOtp(emailOrId)) {
                manager.get().setPassword(passwordEncoder.encode(confirmNewPassword));
                managerRepo.save(manager.get());
                return "Password has Changed!!!";
            } else {
                return "Invalid Credentials";
            }
        }

        Optional<Manager> managerById = managerRepo.findById(emailOrId);
        if (managerById.isPresent()) {
            if (newPassword.equals(confirmNewPassword) && otp == this.otpService.getOtp(emailOrId)) {
                managerById.get().setPassword(passwordEncoder.encode(confirmNewPassword));
                managerRepo.save(managerById.get());
                return "Password has Changed!!!";
            } else {
                return "Invalid Credentials";
            }
        }

        Optional<DeliveryPerson> deliveryPerson = deliveryPersonRepository.findByEmail(emailOrId);
        if (deliveryPerson.isPresent()) {
            if (newPassword.equals(confirmNewPassword) && otp == this.otpService.getOtp(emailOrId)) {
                deliveryPerson.get().setPassword(passwordEncoder.encode(confirmNewPassword));
                deliveryPersonRepository.save(deliveryPerson.get());
                return "Password has Changed!!!";
            } else {
                return "Invalid Credentials";
            }
        }
        return new AuthResponse("Invalid User credentials");
    }
}
