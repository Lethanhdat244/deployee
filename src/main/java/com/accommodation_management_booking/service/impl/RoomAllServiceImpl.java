package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.entity.Bed;
import com.accommodation_management_booking.entity.Dorm;
import com.accommodation_management_booking.entity.Floor;
import com.accommodation_management_booking.entity.Room;
import com.accommodation_management_booking.repository.BedRepository;
import com.accommodation_management_booking.repository.DormRepository;
import com.accommodation_management_booking.repository.FloorRepository;
import com.accommodation_management_booking.repository.RoomRepository;
import com.accommodation_management_booking.service.RoomAllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomAllServiceImpl implements RoomAllService {

    @Autowired
    private DormRepository dormRepo;

    @Autowired
    private FloorRepository floorRepo;

    @Autowired
    private RoomRepository roomRepo;

    @Autowired
    private BedRepository bedRepo;


    @Override
    public List<Dorm> getAllDorms() {
        return dormRepo.findAll();
    }

    @Override
    public List<Floor> getFloorsByDormId(Integer dormId) {
        return floorRepo.findByDormDormId(dormId);
    }



    @Override
    public List<Bed> getBedsByRoomNumber(String roomNumber) {
        List<Room> rooms = roomRepo.findByRoomNumber(roomNumber);
        List<Bed> allBeds = new ArrayList<>();
        if (rooms != null && !rooms.isEmpty()) {
            for (Room room : rooms) {
                allBeds.addAll(bedRepo.findByRoom(room));
            }
        }
        return allBeds;
    }


    public Page<Room> getRoomByDormIdAndFloorNumber(Integer dormId, Integer floorNumber, Pageable pageable) {
        return roomRepo.findByFloorDormDormIdAndFloorFloorNumber(dormId, floorNumber, pageable);

    }


    public List<Dorm> getDormsByGender(Dorm.DormGender gender) {
        return dormRepo.findByDormGender(gender);
    }
}
