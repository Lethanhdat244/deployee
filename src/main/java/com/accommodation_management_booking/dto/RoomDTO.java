package com.accommodation_management_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Integer roomId;
    private Integer floorId;
    private String roomNumber;
    private Integer capacity;
    private Float pricePerBed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}