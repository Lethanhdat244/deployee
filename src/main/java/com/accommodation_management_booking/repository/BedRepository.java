package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BedRepository extends JpaRepository<Bed, Integer> {
    List<Bed> findByRoomRoomIdAndIsAvailableTrue(Integer roomId);

    List<Bed> findByRoomRoomIdAndIsAvailableTrueAndMaintenanceStatus(Integer roomId, Bed.MaintenanceStatus maintenanceStatus);
}
