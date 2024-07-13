package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomsRepository extends JpaRepository<Room, Integer> {
    @Query("SELECT c FROM Room c")
    List<Room> getAllRooms();
}
