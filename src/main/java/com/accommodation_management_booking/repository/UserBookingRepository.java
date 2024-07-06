
package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.dto.UserBookingDTO;
import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBookingRepository extends JpaRepository<User, Integer> {
    @Query("SELECT NEW com.accommodation_management_booking.dto.UserBookingDTO(u.userId, bo.bookingId) " +
            "FROM Room r " +
            "JOIN Bed b ON b.room.roomId = r.roomId " +
            "JOIN Booking bo ON bo.bed.bedId = b.bedId " +
            "JOIN User u ON bo.user.userId = u.userId " +
            "WHERE r.roomId = :roomId AND CURRENT_DATE BETWEEN bo.startDate AND bo.endDate")
    List<UserBookingDTO> findCurrentBookingsByRoomId(@Param("roomId") int roomId);
}
