package com.tiffino.repository;

import com.tiffino.entity.CloudKitchen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CloudKitchenRepository extends JpaRepository<CloudKitchen,String> {
}
