package com.tiffino.repository;

import com.tiffino.entity.User;
import com.tiffino.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByIsSubscribedTrue();

    boolean existsByUser_UserIdAndIsSubscribedTrue(Long userId);

    UserSubscription findByUser_UserIdAndIsSubscribedTrue(Long userId);

    List<UserSubscription> findAllByIsSubscribedTrueAndExpiryDateBefore(LocalDateTime now);

    long countByUser_UserId(Long userId);
}
