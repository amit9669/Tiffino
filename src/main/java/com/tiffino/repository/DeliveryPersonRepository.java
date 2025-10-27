package com.tiffino.repository;

import com.tiffino.entity.DeliveryPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson,Long> {
    List<DeliveryPerson> findByIsAvailableTrue();

    Optional<DeliveryPerson> findByEmail(String email);

    boolean existsByEmail(String email);

    List<DeliveryPerson> findByIsAvailableTrueAndIsActiveTrue();
}
