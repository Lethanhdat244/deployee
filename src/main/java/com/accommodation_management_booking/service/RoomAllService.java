package com.accommodation_management_booking.service;

import com.accommodation_management_booking.entity.Bed;
import com.accommodation_management_booking.entity.Dorm;
import com.accommodation_management_booking.entity.Floor;
import com.accommodation_management_booking.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoomAllService {
    List<Dorm> getAllDorms();

    List<Floor> getFloorsByDormId(Integer dormId);

    Page<Room> getRoomByDormIdAndFloorNumber(Integer dormId, Integer floorNumber, Pageable pageable);

    List<Bed> getBedsByRoomNumber(String roomNumber);

    List<Dorm> getDormsByGender(Dorm.DormGender gender);

}
