package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.entity.Complaint;
import com.accommodation_management_booking.entity.Notification;
import com.accommodation_management_booking.repository.ComplainRepository;
import com.accommodation_management_booking.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    @Autowired
    NotificationRepository notificationRepository;

    public void saveNotification(Notification notification) {
        try {
            notificationRepository.save(notification);
            System.out.println(notification.getUser().getEmail() + "have an unseen notification");  // Debug statement
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send notification", e);
        }
    }
}
