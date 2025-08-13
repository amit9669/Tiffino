package com.tiffino.repository;

import com.tiffino.entity.Manager;
import com.tiffino.entity.response.AdminFilterResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ManagerRepository extends JpaRepository<Manager,String> {

    @Query(value = "SELECT new com.tiffino.entity.response.AdminFilterResponse(ck.cloudKitchenId, ck.state," +
            " ck.city, ck.division, ck.isActive, ck.isDeleted, ck.createdAt, man.managerId," +
            " man.managerName, man.managerEmail, man.dob, man.phoneNo, man.currentAddress, man.permeantAddress," +
            " man.adharCard, man.panCard, man.photo, man.isActive, man.isDeleted, man.createdAt) FROM CloudKitchen AS ck " +
            " LEFT JOIN Manager AS man ON man.cloudKitchen.cloudKitchenId = ck.cloudKitchenId WHERE" +
            " man.isDeleted = false AND ck.isDeleted = false AND" +
            " (COALESCE(:state,'')='' OR TRIM(LOWER(ck.state)) IN (:state)) AND" +
            " (COALESCE(:city,'')='' OR TRIM(LOWER(ck.city)) IN (:city)) AND" +
            " (COALESCE(:division,'')='' OR TRIM(LOWER(ck.division)) IN (:division))",nativeQuery = false)
    List<AdminFilterResponse> getAllDetails(@Param("state") List<String> state,
                                            @Param("city") List<String> city,
                                            @Param("division") List<String> division);

    boolean existsByManagerEmail(String email);

    Optional<Manager> findByManagerEmail(String email);
}
