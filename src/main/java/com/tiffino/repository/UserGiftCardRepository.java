package com.tiffino.repository;

import com.tiffino.entity.DurationType;
import com.tiffino.entity.UserGiftCards;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserGiftCardRepository extends JpaRepository<UserGiftCards, Long> {
    Optional<UserGiftCards> findByGiftCardCodeAndUser_UserIdAndIsRedeemedFalse(String giftCardCode, Long userId);

    Optional<UserGiftCards> findByUser_UserIdAndValidForPlanAndIsRedeemedFalse(Long userId, DurationType validForPlan);

    List<UserGiftCards> findByUser_UserIdAndIsRedeemedFalse(Long userId);
}
