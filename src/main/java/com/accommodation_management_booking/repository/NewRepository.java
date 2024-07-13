package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.New;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NewRepository extends JpaRepository<New, Long> {
    List<New> findTop10ByOrderByNewsIdDesc();
    List<New> findAllByOrderByNewsIdDesc();
    Page<New> findAllByOrderByNewsIdDesc(Pageable pageable);
    Page<New> findByTitleContainingIgnoreCaseOrderByNewsIdDesc(String keyword, Pageable pageable);
}
