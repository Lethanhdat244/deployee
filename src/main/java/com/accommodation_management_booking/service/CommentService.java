package com.accommodation_management_booking.service;

import com.accommodation_management_booking.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    Page<Comment> findPaginated(Pageable pageable);
}
