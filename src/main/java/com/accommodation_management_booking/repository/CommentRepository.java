package com.accommodation_management_booking.repository;

import com.accommodation_management_booking.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query("SELECT c FROM Comment c ORDER BY c.commentId DESC")
    Page<Comment> findPaginated(Pageable pageable);
}
