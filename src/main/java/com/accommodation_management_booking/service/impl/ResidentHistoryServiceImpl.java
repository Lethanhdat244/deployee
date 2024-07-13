package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.dto.ResidentHistoryDTO;
import com.accommodation_management_booking.entity.*;
import com.accommodation_management_booking.repository.BedRepository;
import com.accommodation_management_booking.repository.BookingRepository;
import com.accommodation_management_booking.repository.RoomRepository;
import com.accommodation_management_booking.repository.UserRepository;
import com.accommodation_management_booking.service.ResidentHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResidentHistoryServiceImpl implements ResidentHistoryService {

    private final BookingRepository bookingRepo;
    private final BedRepository bedRepo;
    private final RoomRepository roomRepo;
    private final UserRepository userRepo;


    @Autowired
    public ResidentHistoryServiceImpl(BookingRepository bookingRepo,
                                      BedRepository bedRepo,
                                      RoomRepository roomRepo, UserRepository userRepo) {
        this.bookingRepo = bookingRepo;
        this.bedRepo = bedRepo;
        this.roomRepo = roomRepo;
        this.userRepo = userRepo;
    }



    @Override
    public Page<ResidentHistoryDTO> getUsersResidentHistory(Pageable pageable) {
        User.Role role = User.Role.USER;
        Page<User> userPage = userRepo.findAllByRoleUser(role, pageable);
        List<ResidentHistoryDTO> residentHistoryList = userPage.getContent().stream()
                .map(user -> convertToResidentHistoryDTO(user))
                .collect(Collectors.toList());
        return new PageImpl<>(residentHistoryList, pageable, userPage.getTotalElements());
    }
    @Override
    public Page<ResidentHistoryDTO> searchByUserName(String keyword, Pageable pageable) {
        User.Role role = User.Role.USER;
        Page<User> userPage = userRepo.findByUsernameContainingAndRoleUser(keyword, role, pageable);
        List<ResidentHistoryDTO> residentHistoryList = userPage.getContent().stream()
                .map(this::convertToResidentHistoryDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(residentHistoryList, pageable, userPage.getTotalElements());
    }


    private ResidentHistoryDTO convertToResidentHistoryDTO(User user) {
        ResidentHistoryDTO dto = new ResidentHistoryDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }



    @Override
    public ResidentHistoryDTO findResidentHistoryById(int residentHistoryId) {
        Booking booking = bookingRepo.findById(residentHistoryId).orElseThrow(() ->
                new RuntimeException("Booking not found for id: " + residentHistoryId));
        return convertToResidentHistoryDTO(booking);
    }

    @Override
    public ResidentHistoryDTO getResidentHistoryByUsername(String username) {
        Booking booking = bookingRepo.findByUsernameAndRoleUser(username);
        if (booking == null) {
            throw new RuntimeException("Booking not found for username: " + username);
        }
        return convertToResidentHistoryDTO(booking);
    }

    @Override
    public Page<ResidentHistoryDTO> searchByRoomNumber(String roomNumber, Pageable pageable) {
        Page<Booking> bookings = bookingRepo.findByRoomNumberContainingAndRoleUser(roomNumber, pageable);
        List<ResidentHistoryDTO> residentHistoryList = bookings.getContent().stream()
                .map(this::convertToResidentHistoryDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(residentHistoryList, pageable, bookings.getTotalElements());
    }








    @Override
    public Page<ResidentHistoryDTO> findAllByUserIdOrderByEndDateDesc(int userId, Pageable pageable) {
        Page<Booking> bookings = bookingRepo.findByUserIdOrderByEndDateDescWithBedInfo(userId, pageable);
        List<ResidentHistoryDTO> residentHistoryList = bookings.getContent().stream()
                .map(this::convertToResidentHistoryDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(residentHistoryList, pageable, bookings.getTotalElements());
    }

    private ResidentHistoryDTO convertToResidentHistoryDTO(Booking booking) {
        ResidentHistoryDTO dto = new ResidentHistoryDTO();
        User user = booking.getUser();
        Room room = booking.getRoom();
        Bed bed = booking.getBed();

        if (user != null) {
            dto.setUserId(user.getUserId());

        }

        if (room != null) {
            dto.setRoomNumber(room.getRoomNumber());
            dto.setCapacity(room.getCapacity());
        }

        if (bed != null) {
            dto.setBedName(bed.getBedName());
        }

        dto.setStartDate(booking.getStartDate());
        dto.setEndDate(booking.getEndDate());
        dto.setTotalPrice(booking.getTotalPrice());

        return dto;
    }

}


