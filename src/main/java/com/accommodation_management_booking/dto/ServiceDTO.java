package com.accommodation_management_booking.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private int serviceId;

    private String name;

    @DecimalMin("0.00")
    private BigDecimal price;

    private String description;
}