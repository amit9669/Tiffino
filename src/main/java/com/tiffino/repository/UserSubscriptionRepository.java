package com.tiffino.repository;

import com.tiffino.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription,Long> {
    List<UserSubscription> findByIsSubscribedTrue();
}
