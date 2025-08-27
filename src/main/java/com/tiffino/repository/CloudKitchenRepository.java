package com.tiffino.repository;

import com.tiffino.entity.CloudKitchen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloudKitchenRepository extends JpaRepository<CloudKitchen,String> {
    boolean existsByCloudKitchenIdAndIsDeletedFalse(String kitchenId);

    Optional<CloudKitchen> findByCloudKitchenIdAndIsDeletedFalse(String cloudKitchenId);

    List<CloudKitchen> findAllByIsDeletedFalse();
}
