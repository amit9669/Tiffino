package com.tiffino.service;

import com.tiffino.config.JwtService;
import com.tiffino.entity.*;
import com.tiffino.repository.ManagerRepository;
import com.tiffino.repository.SuperAdminRepository;
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


    public String login(String emailOrId, String rawPassword) {

        Optional<SuperAdmin> superAdmin = superAdminRepo.findByEmail(emailOrId);
        if (superAdmin.isPresent() && passwordEncoder.matches(rawPassword, superAdmin.get().getPassword())) {
            return jwtService.generateToken(emailOrId, Role.SUPER_ADMIN.name());
        }

        if (managerRepo.existsByManagerEmail(emailOrId)) {
            Optional<Manager> manager = managerRepo.findByManagerEmail(emailOrId);
            if (manager.isPresent() && passwordEncoder.matches(rawPassword, manager.get().getPassword())) {
                return jwtService.generateToken(emailOrId, Role.MANAGER.name());
            } else {
                return "Invalid Manager Credentials";
            }
        } else if (managerRepo.existsById(emailOrId)) {
            Optional<Manager> managerId = managerRepo.findById(emailOrId);
            if (managerId.isPresent() && passwordEncoder.matches(rawPassword, managerId.get().getPassword())) {
                return jwtService.generateToken(managerId.get().getManagerEmail(), Role.MANAGER.name());
            } else {
                return "Manager Id Incorrect!";
            }
        }
        return "Invalid credentials";
    }


    public Object getProfile() {
        return dataToken.getCurrentUserProfile();
    }
}
