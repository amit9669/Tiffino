package com.tiffino.repository;

import com.tiffino.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByIsSubscribedTrue();

    List<UserSubscription> findAllByIsSubscribedTrueAndExpiryDateBefore(LocalDateTime now);

    long countByUser_UserId(Long userId);

    boolean existsByUser_UserIdAndIsSubscribedTrue(Long userId);
}
