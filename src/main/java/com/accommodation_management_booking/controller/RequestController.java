package com.accommodation_management_booking.controller;

//import com.accommodation_management_booking.repository.ComplainRepository;
import com.accommodation_management_booking.entity.Complaint;
import com.accommodation_management_booking.entity.New;
import com.accommodation_management_booking.entity.Notification;
import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.ComplainRepository;
import com.accommodation_management_booking.repository.UserRepository;
import com.accommodation_management_booking.service.impl.ComplainService;
import com.accommodation_management_booking.service.impl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class RequestController {
    @Autowired
    ComplainRepository complainRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplainService complainService;

    @Autowired
    private NotificationService notificationService;

    User user;

    @GetMapping("fpt-dorm/user/my-request")
    public String studentRequest(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size,
                                 Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size);
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("email", email);
            user = userRepository.searchUserByEmail(email);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("email", userDetails.getUsername());
            user = userRepository.searchUserByEmail(userDetails.getUsername());
        } else {
            // Handle cases where the authentication is not OAuth2
            model.addAttribute("email", "Unknown");
        }
        try {
            Page<Complaint> complainList = complainRepository.getRequestsByUserId(user.getUserId(), pageable);
            if (complainList.isEmpty()) {
                model.addAttribute("emptyMessage", "No data");
                return "student_request";
            }
            model.addAttribute("complaintDTOList", complainList);
//            Page<Complaint> complaintPage;
//            complaintPage = complainService.getAllComplainByPage(page, size);
//            model.addAttribute("complaintPage", complaintPage);
//            model.addAttribute("currentPage", page);
//            model.addAttribute("pageSize", size);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "student_request";
    }

    @PostMapping("fpt-dorm/user/send-request")
    public String studentRequest(Model model
            , @RequestParam("title") String title
            , @RequestParam("content") String content,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "3") int size
            , Authentication authentication){
        Pageable pageable = PageRequest.of(page, size);
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("email", email);
            user = userRepository.searchUserByEmail(email);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("email", userDetails.getUsername());
            user = userRepository.searchUserByEmail(userDetails.getUsername());
        } else {
            // Handle cases where the authentication is not OAuth2
            model.addAttribute("email", "Unknown");
        }
        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setTitle(title);
        complaint.setDescription(content);
        complaint.setStatus(Complaint.Status.WAITING);
        try {
            List<User> Emp = userRepository.searchAllEmployees();
            complainService.saveComplain(complaint);
//            for (User user : Emp) {
//                Notification notification = new Notification();
//                notification.setUser(user);
//                notification.setContent("New request from your tenant");
//                notification.setRead(false);
//                notificationService.saveNotification(notification);
//            }
            try {
                Page<Complaint> complainList = complainRepository.getRequestsByUserId(user.getUserId(), pageable);
                model.addAttribute("complaintDTOList", complainList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IllegalArgumentException e) {
            return "redirect:/fpt-dorm/user/my-request";
        } catch (Exception e) {
            return "redirect:/fpt-dorm/user/my-request";
        }

        return "redirect:/fpt-dorm/user/my-request";
    }
}
