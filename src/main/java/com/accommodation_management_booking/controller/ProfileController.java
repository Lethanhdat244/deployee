package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProfileController {


    @Autowired
    private UserRepository userRepository;

    @GetMapping("fpt-dorm/user/profile")
    public String profile(@RequestParam("email") String email, Model model) {
        List<User> users = userRepository.searchByEmail(email);
        if (users != null) {
            model.addAttribute("user", users.get(0));
            return "user_profile";
        } else {
            return "error/error";
        }
    }
    @GetMapping("fpt-dorm/admin/profile")
    public String admin_profile(@RequestParam("email") String email, Model model) {
        List<User> users = userRepository.searchByEmail(email);
        if (users != null) {
            model.addAttribute("user", users.get(0));
            return "admin/admin-profile";

        } else {
            return "error/error";
        }
    }

    @GetMapping("fpt-dorm/employee/profile")
    public String employee_profile(@RequestParam("email") String email, Model model) {
        List<User> users = userRepository.searchByEmail(email);
        if (users != null) {
            model.addAttribute("user", users.get(0));
            return "employee/employee-profile";

        } else {
            return "error/error";
        }
    }

}
