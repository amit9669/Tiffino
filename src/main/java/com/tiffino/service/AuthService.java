package com.tiffino.service;

import com.tiffino.config.JwtService;
import com.tiffino.entity.*;
import com.tiffino.entity.response.AuthResponse;
import com.tiffino.repository.DeliveryPersonRepository;
import com.tiffino.repository.ManagerRepository;
import com.tiffino.repository.SuperAdminRepository;
import com.tiffino.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private DeliveryPersonRepository deliveryPersonRepository;


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
        return dataToken.getCurrentUserProfile();
    }
}
