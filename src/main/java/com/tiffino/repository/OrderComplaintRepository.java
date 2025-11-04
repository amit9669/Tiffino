package com.tiffino.repository;

import com.tiffino.entity.OrderComplaint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderComplaintRepository extends JpaRepository<OrderComplaint,Long> {
}
