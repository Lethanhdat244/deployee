package com.accommodation_management_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentHistoryDTO {
    private int userId;
    private String email;

    private String username;
    private String roomNumber;
    private String bedName;
    private LocalDate startDate;
    private String phoneNumber;

    private LocalDate endDate;
    private Float totalPrice;
    private Integer capacity;
}
