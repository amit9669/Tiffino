package com.tiffino.repository;

import com.tiffino.entity.Manager;
import com.tiffino.entity.response.AdminFilterResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, String> {

    @Query("SELECT new com.tiffino.entity.response.AdminFilterResponse(" +
            " ck.cloudKitchenId, ck.city, ck.division, ck.isActive, ck.isDeleted, ck.createdAt," +
            " man.managerId, man.managerName, man.isActive, man.isDeleted, man.createdAt) " +
            "FROM CloudKitchen ck " +
            "LEFT JOIN Manager man ON man.cloudKitchen.cloudKitchenId = ck.cloudKitchenId " +
            "WHERE man.isDeleted = false AND ck.isDeleted = false " +
            "AND (:state IS NULL OR TRIM(LOWER(ck.state)) IN (:state)) " +
            "AND (:city IS NULL OR TRIM(LOWER(ck.city)) IN (:city)) " +
            "AND (:division IS NULL OR TRIM(LOWER(ck.division)) IN (:division))")
    List<AdminFilterResponse> getAllDetails(@Param("state") List<String> state,
                                            @Param("city") List<String> city,
                                            @Param("division") List<String> division);


    boolean existsByManagerEmail(String email);

    Optional<Manager> findByManagerEmail(String email);

    Manager findByCloudKitchen_CloudKitchenId(String cloudKitchenId);

    boolean existsByManagerIdAndIsDeletedFalse(String managerId);

    @Query("SELECT m.managerId FROM Manager m " +
            "WHERE m.managerId LIKE CONCAT(:prefix, '%') " +
            "ORDER BY m.managerId DESC")
    List<String> findLastManagerIdForPrefix(@Param("prefix") String prefix, Pageable pageable);

    List<Manager> findAllByIsDeletedFalse();

}
