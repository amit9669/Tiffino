package com.tiffino.repository;

import com.tiffino.entity.Offers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OffersRepository extends JpaRepository<Offers, Long> {
    List<Offers> findByValidDate(LocalDate today);

    boolean existsByValidDateAndActiveTrue(LocalDate offerDate);

    boolean existsByValidDateBetweenAndActiveTrue(LocalDate localDate, LocalDate localDate1);
}
