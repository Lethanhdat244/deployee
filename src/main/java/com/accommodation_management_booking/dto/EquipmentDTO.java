package com.accommodation_management_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDTO {
    private int equipmentId;

    private int roomId;

    private String name;

    private int quantity;

    private String equipCondition;
}