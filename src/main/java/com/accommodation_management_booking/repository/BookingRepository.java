package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.dto.ResidentHistoryDTO;
import com.accommodation_management_booking.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Integer> {


    @Query("SELECT b FROM Booking b JOIN b.user u WHERE u.roleUser = 'USER'")
    Page<Booking> findAllByRoleUser(Pageable pageable);
    @Query("SELECT b FROM Booking b JOIN b.user u WHERE u.username = :username AND u.roleUser = 'USER'")
    Booking findByUsernameAndRoleUser(@Param("username") String username);

    @Query("SELECT b FROM Booking b WHERE b.bookingId = :id")
    List<Booking> findByBookingId(@Param("id") int id);

    @Query("SELECT b FROM Booking b JOIN b.user u WHERE u.username LIKE %:username%")
    List<Booking> findByUserName(@Param("username") String username);

    @Query("SELECT b FROM Booking b JOIN b.user u WHERE u.username LIKE %:keyword% OR b.bookingId = :id")
    List<Booking> searchAllBy(@Param("keyword") String keyword, @Param("id") int id);

    @Query("SELECT b FROM Booking b JOIN Bed bd ON b.bed.bedId = bd.bedId WHERE b.user.userId = :userId ORDER BY b.endDate DESC")
    Page<Booking> findByUserIdOrderByEndDateDescWithBedInfo(@Param("userId") int userId, Pageable pageable);

    @Query("SELECT b FROM Booking b JOIN b.room r JOIN b.user u WHERE r.roomNumber LIKE %:roomNumber% AND u.roleUser = 'USER'")
    Page<Booking> findByRoomNumberContainingAndRoleUser(@Param("roomNumber") String roomNumber, Pageable pageable);


}
