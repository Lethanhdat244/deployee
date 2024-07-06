package com.accommodation_management_booking.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private int paymentId;

    private int bookingId;

    @DecimalMin("0.00")
    private BigDecimal amount;

    private LocalDate paymentDate;

    private String paymentMethod;

    private String paymentStatus;
}