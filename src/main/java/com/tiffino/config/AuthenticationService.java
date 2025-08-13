package com.tiffino.config;


import com.tiffino.repository.ManagerRepository;
import com.tiffino.repository.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthenticationService {

    @Autowired
    private ManagerRepository managerRepository;

    private final SuperAdminRepository superAdminRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            SuperAdminRepository superAdminRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.superAdminRepository = superAdminRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    public SuperAdmin authenticate(AdminLogIn request) {
//
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//
//
//            return superAdminRepository.findByEmail(request.getEmail())
//                    .orElseThrow();
//    }
}
