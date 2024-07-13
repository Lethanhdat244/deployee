package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ChangePasswordController {

    private final UserService userService;

    public ChangePasswordController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("fpt-dorm/change-password")
    public String showChangePasswordPage() {
        return "change_password";
    }

    @PostMapping("fpt-dorm/change-password")
    public String changePassword(
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmNewPassword") String confirmNewPassword,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addAttribute("passwordMismatch", "true");
            return "redirect:/fpt-dorm/change-password";
        }

        boolean success = userService.changePassword(currentPassword, newPassword);

        if (success) {
            redirectAttributes.addAttribute("success", "true");
        } else {
            redirectAttributes.addAttribute("error", "true");
        }

        return "redirect:/fpt-dorm/change-password";
    }
}

