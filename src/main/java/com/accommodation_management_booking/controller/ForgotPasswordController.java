package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@AllArgsConstructor
public class ForgotPasswordController {

    private final UserService userService;

    @GetMapping("fpt-dorm/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("fpt-dorm/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        try {
            userService.processForgotPassword(email);
            model.addAttribute("message", "A reset password link has been sent to your email.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "forgot_password";
        }
        return "redirect:/fpt-dorm/forgot-password?success";
    }
}
