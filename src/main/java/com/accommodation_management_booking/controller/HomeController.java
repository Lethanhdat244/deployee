package com.accommodation_management_booking.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/fpt-dorm")
    public String redirectToHome() {
        return "redirect:/fpt-dorm/home";
    }

    @GetMapping("fpt-dorm/home")
    public String homepage(){
        return "homepage";
    }


    @GetMapping("fpt-dorm/home/about")
    public String about(){
        return "about";
    }

    @GetMapping("fpt-dorm/home/gallery")
    public String gallery(){
        return "gallery";
    }


    @GetMapping("fpt-dorm/home/contact")
    public String contact(){
        return "contact";
    }

    @GetMapping("fpt-dorm/user/rule")
    public String rule(){
        return "rule";
    }
}
