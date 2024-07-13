package com.accommodation_management_booking.service;

import com.accommodation_management_booking.dto.ResidentHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResidentHistoryService {
    Page<ResidentHistoryDTO> getUsersResidentHistory(Pageable pageable);
    Page<ResidentHistoryDTO> searchByUserName(String keyword, Pageable pageable);
    ResidentHistoryDTO findResidentHistoryById(int residentHistoryId);
    ResidentHistoryDTO getResidentHistoryByUsername(String username);
    Page<ResidentHistoryDTO> findAllByUserIdOrderByEndDateDesc(int userId, Pageable pageable);
    Page<ResidentHistoryDTO> searchByRoomNumber(String roomNumber, Pageable pageable);



}
