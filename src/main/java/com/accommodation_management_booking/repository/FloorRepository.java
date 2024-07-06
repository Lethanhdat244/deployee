package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Integer> {
    List<Floor> findByDormDormId(Integer dormId);
}
