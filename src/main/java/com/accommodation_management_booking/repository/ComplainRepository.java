package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.dto.ComplaintDTO;
import com.accommodation_management_booking.entity.Complaint;
import com.accommodation_management_booking.entity.New;
import com.accommodation_management_booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComplainRepository extends JpaRepository<Complaint, Long> {
    @Query("SELECT c FROM Complaint c")
    List<Complaint> getAllRequest();

    @Query("SELECT c FROM Complaint c WHERE c.status = :status")
    List<Complaint> findDoneComplaints(@Param("status") Complaint.Status status);

    @Query("SELECT c FROM Complaint c WHERE c.user.userId = :userid ORDER BY c.createdAt desc")
    Page<Complaint> getRequestsByUserId(@Param("userid") int userid, Pageable pageable);

    @Query("SELECT c FROM Complaint c WHERE c.complaintId = :id")
    Complaint getRequestByComplaintId(@Param("id") int id);

}
