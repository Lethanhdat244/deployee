package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {
    List<Service> findAllByServiceId(int serviceId);
}
