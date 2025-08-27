package com.tiffino.repository;

import com.tiffino.entity.UserOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserOfferRepository extends JpaRepository<UserOffer, Long> {

    boolean existsByUser_UserIdAndOffer_OfferId(Long userId, Long offerId);

    Optional<UserOffer> findByUser_UserIdAndOffer_OfferId(Long userId, Long offerId);

    List<UserOffer> findByUser_UserId(Long userId);
}
