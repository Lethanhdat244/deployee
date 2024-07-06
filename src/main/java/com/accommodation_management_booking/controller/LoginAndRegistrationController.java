package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.dto.UserDTO;
import com.accommodation_management_booking.service.EmailService;
import com.accommodation_management_booking.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@AllArgsConstructor
public class LoginAndRegistrationController {

    private UserService userService;
    private EmailService emailService;

    @GetMapping("fpt-dorm/register")
    public String registerUser(Model model) {
        UserDTO user = new UserDTO();
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping("fpt-dorm/register/save")
    public String registration(@ModelAttribute("user") UserDTO userDTO,
                               BindingResult result,
                               Model model,
                               @RequestParam("avatar") MultipartFile[] avatars,
                               @RequestParam("frontface") MultipartFile[] frontCccdImages,
                               @RequestParam("backface") MultipartFile[] backCccdImages) {

        try {
            userService.saveUser(userDTO, avatars, frontCccdImages, backCccdImages);
            String emailContent = String.format(
                    "<p>Dear %s,</p>" +
                            "<p>Thank you for registering at Booking Dorm.</p>" +
                            "<table>" +
                            "<tr><td>Username:</td><td>%s</td></tr>" +
                            "<tr><td>Address:</td><td>%s</td></tr>" +
                            "<tr><td>Phone:</td><td>%s</td></tr>" +
                            "<tr><td>CCCD Number:</td><td>%s</td></tr>" +
                            "</table>" +
                            "<p>Thank you for using our service.</p>" +
                            "<p><a href=\"http://localhost:8080/fpt-dorm/home\">Go to Home</a></p>",
                    userDTO.getUsername(), userDTO.getUsername(), userDTO.getAddress(),
                    userDTO.getPhoneNumber(), userDTO.getCccdNumber()
            );

            emailService.sendRegistrationEmail(userDTO.getEmail(), emailContent);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "There was an error registering the user. Please try again.");
            return "register";
        }
        return "redirect:/fpt-dorm/register?success";
    }

    @GetMapping("/fpt-dorm/login")
    public String login(){
        return "login";
    }

}
