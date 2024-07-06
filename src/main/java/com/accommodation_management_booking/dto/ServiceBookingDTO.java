package com.accommodation_management_booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceBookingDTO {
    private int serviceBookingId;

    private int bookingId;

    private int serviceId;

    private int quantity;
}