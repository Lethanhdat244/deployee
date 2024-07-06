package com.accommodation_management_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRequestDTO {
    private int requestId;

    private int userId;

    private int roomId;

    private String description;

    private String status;

    private Integer assignedTo;

    private LocalDate completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}