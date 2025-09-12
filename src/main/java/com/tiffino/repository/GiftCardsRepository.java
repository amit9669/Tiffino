package com.tiffino.repository;

import com.tiffino.entity.GiftCards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GiftCardsRepository extends JpaRepository<GiftCards, Long> {
    Optional<GiftCards> findByTypeAndIsActiveTrue(String loyaltyPoints);
}
