package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.entity.Complaint;
import com.accommodation_management_booking.entity.New;
import com.accommodation_management_booking.repository.ComplainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ComplainService {
    @Autowired
    ComplainRepository complainRepository;
    public void saveComplain(Complaint complaint) {
        try {
            complainRepository.save(complaint);
            System.out.println("Complain saved successfully with title: " + complaint.getDescription());  // Debug statement
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save complain", e);
        }
    }

    public Page<Complaint> getAllComplainByPage(int page, int size) {
        return complainRepository.findAll(PageRequest.of(page, size));
    }
}
