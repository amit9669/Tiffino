package com.tiffino.service;

import com.tiffino.repository.ManagerRepository;
import com.tiffino.repository.SuperAdminRepository;
import com.tiffino.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataToken {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private ManagerRepository managerRepository;

    @Autowired
    private UserRepository userRepository;

    public Object getCurrentUserProfile() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            String email = (String) auth.getPrincipal();
            String role = auth.getAuthorities().iterator().next().getAuthority(); // e.g., ROLE_SUPER_ADMIN

            switch (role) {
                case "ROLE_SUPER_ADMIN":
                    return superAdminRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("SuperAdmin not found"));
                case "ROLE_MANAGER":
                    return managerRepository.findByManagerEmail(email)
                            .or(() -> managerRepository.findByManagerEmail(email))
                            .orElseThrow(() -> new RuntimeException("Manager not found"));
                case "ROLE_USER":
                    return userRepository.findByEmail(email)
                            .or(() -> userRepository.findByEmail(email))
                            .orElseThrow(() -> new RuntimeException("User not found"));
                default:
                    throw new RuntimeException("Unknown role: " + role);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user profile from token: " + e.getMessage());
        }
    }
}
