package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.dto.PaymentTransactionDTO;
import com.accommodation_management_booking.entity.Booking;
import com.accommodation_management_booking.entity.Payment;
import com.accommodation_management_booking.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByBooking(Booking booking);

    @Query("SELECT DISTINCT u FROM User u " +
            "INNER JOIN Booking b ON u.userId = b.user.userId " +
            "INNER JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE u.roleUser = 'USER'")
    Page<User> findUsersWithBooking(Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Booking b ON u.userId = b.user.userId " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE u.roleUser = 'USER' " +
            "AND (" +
            "   LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            ")")
    Page<User> searchAllInUsersWithBooking(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Booking b ON u.userId = b.user.userId " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE u.userId = :userId")
    Page<User> searchByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE u.userId = :userId")
    Page<PaymentTransactionDTO> searchPaymentByUserId(@Param("userId") Integer userId, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Booking b " +
            "JOIN b.user u " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE u.userId = :userId")
    Page<PaymentTransactionDTO> searchBookingByUserId(@Param("userId") Integer userId, Pageable pageable);


    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Booking b ON u.userId = b.user.userId " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    Page<User> searchByEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Booking b ON u.userId = b.user.userId " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE LOWER(u.phoneNumber) LIKE LOWER(CONCAT('%', :phoneNumber, '%'))")
    Page<User> searchByPhoneNumber(@Param("phoneNumber") String phoneNumber, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN Booking b ON u.userId = b.user.userId " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    Page<User> searchByUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM User u " +
            "JOIN Booking b ON u.userId = b.user.userId " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE p.paymentId = :paymentId")
    PaymentTransactionDTO findByPaymentId(@Param("paymentId") Integer paymentId);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE b.status = :status")
    Page<PaymentTransactionDTO> searchPaymentByStatus(@Param("status") Booking.Status status, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Booking b " +
            "JOIN b.user u " +
            "JOIN Payment p ON b.bookingId = p.booking.bookingId " +
            "WHERE b.status = :status")
    Page<PaymentTransactionDTO> searchBookingByStatus(@Param("status") Booking.Status status, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE u.email = :email AND b.status = 'Pending'")
    Page<PaymentTransactionDTO> findPendingPaymentsByUserEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE u.email = :email")
    Page<PaymentTransactionDTO> findPaymentsByUserEmail(@Param("email") String email, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE p.paymentId = :paymentId")
    Page<PaymentTransactionDTO> findByPaymentIdWithPaging(@Param("paymentId") Integer paymentId, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE u.userId = :userId AND DATE(p.paymentDate) = :paymentDate")
    Page<PaymentTransactionDTO> findByPaymentDateWithPaging(@Param("paymentDate") LocalDate paymentDate, @Param("userId") int userId, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE b.status = 'Pending' AND p.paymentId = :paymentId")
    Page<PaymentTransactionDTO> findPaymentRequestByPaymentIdWithPage(@Param("paymentId") Integer paymentId, Pageable pageable);

    @Query("SELECT new com.accommodation_management_booking.dto.PaymentTransactionDTO(" +
            "u.userId, u.username, u.email, u.phoneNumber, " +
            "p.paymentId, p.paymentDate, p.paymentMethod, p.paymentDetail, " +
            "b.bookingId, b.bed.bedId, b.startDate, b.endDate, b.totalPrice, b.amountPaid, b.refundAmount, b.refundDate, " +
            "b.bookingDate, b.status) " +
            "FROM Payment p " +
            "JOIN Booking b ON p.booking.bookingId = b.bookingId " +
            "JOIN User u ON b.user.userId = u.userId " +
            "WHERE b.status = 'Pending' AND DATE(p.paymentDate) = :paymentDate")
    Page<PaymentTransactionDTO> findPaymentRequestByPaymentDateWithPage(@Param("paymentDate") LocalDate paymentDate, Pageable pageable);

    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
}
