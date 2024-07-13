package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.dto.UserBookingDTO;
import com.accommodation_management_booking.entity.UsageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsageServiceRepository extends JpaRepository<UsageService, Integer> {
    @Query(value = "SELECT * FROM usage_service WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    UsageService getCurrentUsageService(@Param("userId") int userId);

    @Query("SELECT us FROM UsageService us " +
            "WHERE us.user.userId = :userId" +
            " order by us.createdAt DESC")
    Page<UsageService> getUsageServicesByUserId(@Param("userId") int userId, Pageable pageable);

    @Query("SELECT u.userId, bo.bookingId FROM User u " +
            "JOIN Booking bo ON u.userId = bo.user.userId " +
            "JOIN Bed b ON bo.bed.bedId = b.bedId " +
            "JOIN Room r ON b.room.roomId = r.roomId " +
            "WHERE r.roomId = :roomId AND CURRENT_DATE BETWEEN bo.startDate AND bo.endDate")
    Page<UserBookingDTO> findUsersByRoomIdAndCurrentDate(@Param("roomId") Integer roomId, Pageable pageable);
}