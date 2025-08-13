package com.tiffino.repository;

import com.tiffino.entity.SuperAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuperAdminRepository extends JpaRepository<SuperAdmin,Long> {
    Optional<SuperAdmin> findByEmail(String username);

    boolean existsByEmail(String email);
}
