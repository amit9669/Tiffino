package com.tiffino.config;

import com.tiffino.repository.ManagerRepository;
import com.tiffino.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SuperAdminRepository superAdminRepository;
    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return superAdminRepository.findByEmail(email)
                .map(sa -> new User(sa.getEmail(), sa.getPassword(),
                        List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))))
                .or(() -> managerRepository.findByManagerEmail(email)
                        .map(m -> new User(m.getManagerEmail(), m.getPassword(),
                                List.of(new SimpleGrantedAuthority("ROLE_MANAGER")))))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
