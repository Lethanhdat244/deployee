package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.entity.Comment;
import com.accommodation_management_booking.repository.CommentRepository;
import com.accommodation_management_booking.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @GetMapping("/fpt-dorm/home/dinning")
    public String showDiningPage(Model model,
                                 @RequestParam(value = "page", defaultValue = "1") int page,
                                 @RequestParam(value = "size", defaultValue = "3") int size) {
        Page<Comment> commentPage = commentRepository.findPaginated(PageRequest.of(page - 1, size));
        model.addAttribute("comments", commentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", commentPage.getTotalPages());
        return "dinning";
    }

    @PostMapping("/fpt-dorm/home/dinning/comments")
    public String addComment(@RequestParam String name, @RequestParam String email, @RequestParam String message, Model model) {
        Comment newComment = new Comment();
        newComment.setName(name);
        newComment.setEmail(email);
        newComment.setMessage(message);
        commentRepository.save(newComment);
        return "redirect:/fpt-dorm/home/dinning";
    }
}
