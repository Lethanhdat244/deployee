package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Notification;
import com.accommodation_management_booking.entity.UsageService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    @Query("SELECT n FROM Notification n where n.user.userId = :id")
    List<Notification> getAllByUserId(@Param("id") int id);
}
