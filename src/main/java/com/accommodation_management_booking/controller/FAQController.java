package com.accommodation_management_booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FAQController {
    @GetMapping("/fpt-dorm/user/faq")
    public String faq(){
        return "student_faq";
    }
}
