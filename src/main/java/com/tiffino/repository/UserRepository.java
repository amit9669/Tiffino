package com.tiffino.repository;

import com.tiffino.entity.User;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.subscriptions")
    List<User> findAllWithSubscriptions();

    Optional<User> findByPhoneNo(@Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits") String phoneNo);
}