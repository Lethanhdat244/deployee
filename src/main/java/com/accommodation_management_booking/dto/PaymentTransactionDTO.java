package com.accommodation_management_booking.dto;

import com.accommodation_management_booking.entity.Booking;
import com.accommodation_management_booking.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionDTO {
    private Integer userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Integer paymentId;
    private LocalDateTime paymentDate;
    private Payment.PaymentMethod paymentMethod;
    private String paymentDetail;
    private Integer bookingId;
    private Integer bedId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Float totalPrice;
    private Float amountPaid;
    private Float refundAmount;
    private LocalDate refundDate;
    private LocalDateTime bookingDate;
    private Booking.Status status;
}
