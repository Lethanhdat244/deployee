package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.entity.Complaint;
import com.accommodation_management_booking.entity.UsageService;
import com.accommodation_management_booking.repository.UsageServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceService {
    @Autowired
    UsageServiceRepository usageServiceRepository;

    public void saveUsageService(UsageService usageService) {
        try {
            usageServiceRepository.save(usageService);
            System.out.println("Send usage service record to " + usageService.getUser().getEmail());  // Debug statement
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save complain", e);
        }
    }
}
