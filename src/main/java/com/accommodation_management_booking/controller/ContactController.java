package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ContactController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/fpt-dorm/contact")
    public String sendContactMessage(@RequestParam("Name") String name,
                                     @RequestParam("Email") String email,
                                     @RequestParam("Message") String message,
                                     Model model) {
        String content = "Name: " + name + "\n" +
                "Email: " + email + "\n" +
                "Message: " + message;
        emailService.sendMailGuess("datlthe176015@fpt.edu.vn", content);
        model.addAttribute("successMessage", "Your message has been sent successfully. Please pay attention!" +
                " We will respond as soon as possible.");
        return "contact";
    }
}
