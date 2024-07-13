package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.dto.PaymentTransactionDTO;
import com.accommodation_management_booking.entity.Booking;
import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.PaymentRepository;
import com.accommodation_management_booking.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;

    public Page<User> getPayments(Pageable pageable) {
        return paymentRepository.findUsersWithBooking(pageable);
    }

    public Page<User> searchAll(String keyword, Pageable pageable) {
        return paymentRepository.searchAllInUsersWithBooking(keyword, pageable);
    }

    public Page<User> searchByUser(int userId, Pageable pageable) {
        return paymentRepository.searchByUserId(userId, pageable);
    }

    public Page<User> searchByName(String keyword, Pageable pageable) {
        return paymentRepository.searchByUsername(keyword, pageable);
    }

    public Page<User> searchByMail(String keyword, Pageable pageable) {
        return paymentRepository.searchByEmail(keyword, pageable);
    }

    public Page<User> searchByPhone(String keyword, Pageable pageable) {
        return paymentRepository.searchByPhoneNumber(keyword, pageable);
    }

    public Page<PaymentTransactionDTO> searchByUserWithPaymentSort(int userId, Pageable pageable) {
        return paymentRepository.searchPaymentByUserId(userId, pageable);
    }

    public Page<PaymentTransactionDTO> searchByUserWithBookingSort(int userId, Pageable pageable) {
        return paymentRepository.searchBookingByUserId(userId, pageable);
    }

    public PaymentTransactionDTO findByPaymentId(int id) {
        return paymentRepository.findByPaymentId(id);
    }

    public Page<PaymentTransactionDTO> searchByStatusWithPaymentSort(Booking.Status status, Pageable pageable) {
        return paymentRepository.searchPaymentByStatus(status, pageable);
    }

    public Page<PaymentTransactionDTO> searchByStatusWithBookingSort(Booking.Status status, Pageable pageable) {
        return paymentRepository.searchBookingByStatus(status, pageable);
    }

    public Page<PaymentTransactionDTO> findPendingPaymentsByUserEmail(String email, Pageable pageable) {
        return paymentRepository.findPendingPaymentsByUserEmail(email, pageable);
    }

    public Page<PaymentTransactionDTO> findPaymentsByUserEmail(String email, Pageable pageable) {
        return paymentRepository.findPaymentsByUserEmail(email, pageable);
    }

    public Page<PaymentTransactionDTO> findByPaymentIdWithPage(int paymentId, Pageable pageable) {
        return paymentRepository.findByPaymentIdWithPaging(paymentId, pageable);
    }

    public Page<PaymentTransactionDTO> findByPaymentDateWithPage(LocalDate paymentDate, int userId, Pageable pageable) {
        return paymentRepository.findByPaymentDateWithPaging(paymentDate, userId, pageable);
    }

    public Page<PaymentTransactionDTO> findPaymentRequestByPaymentId(int paymentId, Pageable pageable) {
        return paymentRepository.findPaymentRequestByPaymentIdWithPage(paymentId, pageable);
    }

    public Page<PaymentTransactionDTO> findPaymentRequestByPaymentDate(LocalDate paymentDate, Pageable pageable) {
        return paymentRepository.findPaymentRequestByPaymentDateWithPage(paymentDate, pageable);
    }
}
