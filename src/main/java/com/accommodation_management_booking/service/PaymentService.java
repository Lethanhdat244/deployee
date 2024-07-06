package com.accommodation_management_booking.service;

import com.accommodation_management_booking.dto.PaymentTransactionDTO;
import com.accommodation_management_booking.entity.Booking;
import com.accommodation_management_booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface PaymentService {
    Page<User> getPayments(Pageable pageable);

    Page<User> searchAll(String keyword, Pageable pageable);

    Page<User> searchByUser(int userId, Pageable pageable);

    Page<User> searchByName(String keyword, Pageable pageable);

    Page<User> searchByMail(String keyword, Pageable pageable);

    Page<User> searchByPhone(String keyword, Pageable pageable);

    Page<PaymentTransactionDTO> searchByUserWithPaymentSort(int userId, Pageable pageable);

    Page<PaymentTransactionDTO> searchByUserWithBookingSort(int userId, Pageable pageable);

    PaymentTransactionDTO findByPaymentId(int id);

    Page<PaymentTransactionDTO> searchByStatusWithPaymentSort(Booking.Status status, Pageable pageable);

    Page<PaymentTransactionDTO> searchByStatusWithBookingSort(Booking.Status status, Pageable pageable);

    Page<PaymentTransactionDTO> findPendingPaymentsByUserEmail(String email, Pageable pageable);

    Page<PaymentTransactionDTO> findPaymentsByUserEmail(String email, Pageable pageable);

    Page<PaymentTransactionDTO> findByPaymentIdWithPage(int paymentId, Pageable pageable);

    Page<PaymentTransactionDTO> findByPaymentDateWithPage(LocalDate paymentDate, int userId, Pageable pageable);

    Page<PaymentTransactionDTO> findPaymentRequestByPaymentId(int paymentId, Pageable pageable);

    Page<PaymentTransactionDTO> findPaymentRequestByPaymentDate(LocalDate paymentDate, Pageable pageable);
}
