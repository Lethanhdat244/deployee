package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.entity.Comment;
import com.accommodation_management_booking.repository.CommentRepository;
import com.accommodation_management_booking.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Override
    public Page<Comment> findPaginated(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

}
