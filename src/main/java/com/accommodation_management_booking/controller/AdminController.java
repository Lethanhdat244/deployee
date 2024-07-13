package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.dto.UserBookingDTO;
import com.accommodation_management_booking.entity.Complaint;
import com.accommodation_management_booking.entity.Dorm;
import com.accommodation_management_booking.entity.Notification;
import com.accommodation_management_booking.entity.UsageService;
import com.accommodation_management_booking.repository.ComplainRepository;
import com.accommodation_management_booking.repository.DormRepository;
import com.accommodation_management_booking.repository.UserBookingRepository;
import com.accommodation_management_booking.repository.UserRepository;
import com.accommodation_management_booking.service.impl.ComplainService;
import com.accommodation_management_booking.service.impl.UsageServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    ComplainRepository complainRepository;
    @Autowired
    ComplainService complainService;
    @Autowired
    DormRepository dormRepository;
    @Autowired
    UserBookingRepository userBookingRepository;
    @Autowired
    UsageServiceService usageServiceService;
    @Autowired
    UserRepository userRepository;

    @GetMapping("fpt-dorm/admin/home")
    public String admin_homepage(Model model, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("email", email);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("email", userDetails.getUsername());
        } else {
            // Handle cases where the authentication is not OAuth2
            model.addAttribute("email", "Unknown");
        }
        return "admin/admin_homepage";
    }

    @GetMapping("fpt-dorm/admin/admin_list_student")
    public String admin_list_students() {
        return "redirect:/fpt-dorm/admin/student/all-student";
    }

    @GetMapping("fpt-dorm/admin/admin_list_employees")
    public String admin_list_employees() {
        return "redirect:/fpt-dorm/admin/employee/all-employee";
    }

    @GetMapping("fpt-dorm/admin/admin_add_student")
    public String admin_add_student() {
        return "redirect:/fpt-dorm/admin/student/add";
    }

    @GetMapping("fpt-dorm/admin/admin_add_employee")
    public String admin_add_employee() {
        return "redirect:/fpt-dorm/admin/employee/add";
    }

    @GetMapping("fpt-dorm/admin/admin_list_room")
    public String admin_list_room() {
        return "admin_list_room";
    }

    @GetMapping("fpt-dorm/admin/admin_payment_list")
    public String admin_payment_list() {
        return "redirect:/fpt-dorm/admin/all-payment";
    }

    @GetMapping("fpt-dorm/admin/admin_payment_request")
    public String admin_payment_request() {
        return "redirect:/fpt-dorm/admin/payment-request";
    }

    @GetMapping("fpt-dorm/admin/admin_add_new_type_room")
    public String admin_add_new_type_room() {
        return "admin_add_new_type_room";
    }

    @GetMapping("fpt-dorm/admin/admin_add_new_room")
    public String admin_add_new_room() {
        return "admin_add_new_room";
    }

    @GetMapping("fpt-dorm/admin/admin_list_feedback")
    public String admin_list_feedback() {
        return "admin_list_feedback";
    }

    @GetMapping("fpt-dorm/admin/admin_list_complaint")
    public String admin_complain(Model model, @RequestParam(name = "status", required = false) Complaint.Status status, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("email", email);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("email", userDetails.getUsername());
        } else {
            // Handle cases where the authentication is not OAuth2
            model.addAttribute("email", "Unknown");
        }
        try {
            List<Complaint> complainList;
            if (status != null) {
                // Filter complainList based on status
                complainList = complainRepository.findDoneComplaints(status);
            } else {
                // If no status is selected, get all complaints
                complainList = complainRepository.findAll();
            }
            if (complainList.isEmpty()) {
                // Handle case where complainList is empty
                model.addAttribute("message", "No complaints found with the selected status.");
                // Optionally, you can redirect to another page or render different view
                // return "redirect:/someOtherPage";
            } else {
                model.addAttribute("complaintDTOList", complainList);
            }
            model.addAttribute("statusForm", status);
            return "admin/admin_list_complaint";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }

    @GetMapping("/fpt-dorm/admin/complain/execute/{id}")
    public String executeComplain(@PathVariable("id") int id, Model model) {
        var complain = complainRepository.getRequestByComplaintId(id);
        model.addAttribute("complainObj", complain);
        return "admin/execute_complain";
    }

    @PostMapping("/fpt-dorm/admin/complain/execute/{id}")
    public String executeComplain(Model model, @PathVariable("id") int id, @RequestParam("status") Complaint.Status status, @RequestParam("reply") String reply) {
        Complaint existComplaint = complainRepository.getRequestByComplaintId(id);
        if (existComplaint != null) {
            existComplaint.setStatus(status);
            existComplaint.setReply(reply);
            complainService.saveComplain(existComplaint);
            Notification notification = new Notification();
            notification.setUser(existComplaint.getUser());
            notification.setContent("Your request was replied");
            notification.setRead(false);
//            notificationService.saveNotification(notification);
            try {
                List<Complaint> complainList = complainRepository.getAllRequest();
                model.addAttribute("complaintDTOList", complainList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "admin/admin_list_complaint";
        } else {
            return "error/403";
        }
    }


    @GetMapping("/fpt-dorm/admin/admin_Resident_History")
    public String admin_list_residentH() {
        return "redirect:/fpt-dorm/admin/Resident_History/list";
    }

    @GetMapping("/fpt-dorm/admin/usage-service")
    public String showListUsageService(Model model, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("email", email);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("email", userDetails.getUsername());
        } else {
            // Handle cases where the authentication is not OAuth2
            model.addAttribute("email", "Unknown");
        }
        List<Dorm> dorms = dormRepository.findAll();
        model.addAttribute("dorms", dorms);
        return "admin/admin_usageService";
    }

    @PostMapping("/fpt-dorm/admin/usage-service/{id}")
    public String executeUsageServiceData(@PathVariable(name = "id") int id,
                                          @RequestParam("electric") int electric,
                                          @RequestParam("water") int water,
                                          @RequestParam("others") int others,
                                          Model model,
                                          Authentication authentication) {
        List<UserBookingDTO> usageServiceDTOs = userBookingRepository.findCurrentBookingsByRoomId(id);
        if (usageServiceDTOs.isEmpty()) {
            model.addAttribute("error", "This room is currently unoccupied.");
            List<Dorm> dorms = dormRepository.findAll();
            model.addAttribute("dorms", dorms);
            return "admin/admin_usageService";
        }

        float e = (electric * 4000f) / usageServiceDTOs.size();
        float w = (water * 5000f) / usageServiceDTOs.size();
        float o = (others * 1000f) / usageServiceDTOs.size();

        for (UserBookingDTO user : usageServiceDTOs) {
            UsageService usageService = new UsageService();
            usageService.setUser(userRepository.searchUserById(user.getUserId()));
            usageService.setBookingId(user.getBookingId());
            usageService.setElectricity(e);
            usageService.setWater(w);
            usageService.setOthers(o);
            usageServiceService.saveUsageService(usageService);
        }

        String email = null;
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            email = userDetails.getUsername();
        }
        model.addAttribute("email", email != null ? email : "Unknown");
        List<Dorm> dorms = dormRepository.findAll();
        model.addAttribute("dorms", dorms);
        return "admin/admin_usageService";
    }

    @GetMapping("/fpt-dorm/admin/admin_all_rooms")
    public String admin_list_all_rooms() {
        return "redirect:/fpt-dorm/admin/all-room";
    }
}
