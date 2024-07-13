package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Dorm;
import com.accommodation_management_booking.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findByFloorFloorId(Integer floorId);

    List<Room> findByFloorFloorIdAndCapacity(Integer floorId, Integer capacity);
    List<Room> findByRoomNumber(String roomNumber);

    Page<Room> findByFloorDormDormIdAndFloorFloorNumber(Integer dormId, Integer floorNumber, Pageable pageable);

}
