package com.tiffino.repository;

import com.tiffino.entity.CloudKitchen;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CloudKitchenRepository extends JpaRepository<CloudKitchen,String> {
    boolean existsByCloudKitchenIdAndIsDeletedFalse(String kitchenId);

    Optional<CloudKitchen> findByCloudKitchenIdAndIsDeletedFalse(String cloudKitchenId);

    List<CloudKitchen> findAllByIsDeletedFalse();

    @Query("SELECT c.cloudKitchenId FROM CloudKitchen c " +
            "WHERE c.cloudKitchenId LIKE CONCAT(:prefix, '%') " +
            "ORDER BY c.cloudKitchenId DESC")
    List<String> findLastCloudKitchenIdForPrefix(@Param("prefix") String prefix, Pageable pageable);
}
