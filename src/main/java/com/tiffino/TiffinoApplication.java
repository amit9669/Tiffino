package com.tiffino;

import com.tiffino.entity.SuperAdmin;
import com.tiffino.repository.SuperAdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class TiffinoApplication implements CommandLineRunner {

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(TiffinoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        saveAdmin();
    }

    private void saveAdmin() {
        if (superAdminRepository.findByEmail("admin@gmail.com").isEmpty()) {
            SuperAdmin superAdmin = new SuperAdmin();
            superAdmin.setAdminName("Admin");
            superAdmin.setEmail("admin@gmail.com");
            superAdmin.setPassword(passwordEncoder.encode("admin"));
            superAdminRepository.save(superAdmin);
            System.out.println("Default super admin created ✅");
        } else {
            System.out.println("Super admin already exists ⚠️");
        }
    }
}
