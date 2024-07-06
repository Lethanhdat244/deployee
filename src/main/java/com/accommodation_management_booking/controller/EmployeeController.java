package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.dto.UserBookingDTO;
import com.accommodation_management_booking.dto.UserDTO;
import com.accommodation_management_booking.entity.*;
import com.accommodation_management_booking.repository.*;
import com.accommodation_management_booking.service.impl.ComplainService;
import com.accommodation_management_booking.service.impl.NotificationService;
import com.accommodation_management_booking.service.impl.UsageServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.accommodation_management_booking.dto.UserDTO;
import com.accommodation_management_booking.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

import java.util.List;

@Controller
@AllArgsConstructor
public class EmployeeController {
    @Autowired
    private UserRepository repo;

    private final UserService userService;

    @Autowired
    ComplainRepository complainRepository;

    @Autowired
    private ComplainService complainService;

    @Autowired
    private RoomsRepository roomsRepository;

    @Autowired
    private UserBookingRepository userBookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UsageServiceService usageServiceService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    DormRepository dormRepository;

    @GetMapping("fpt-dorm/employee/home")
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
        return "employee/employee_homepage";
    }

    @GetMapping("fpt-dorm/employee/complain")
    public String employeeComplain(Model model, @RequestParam(name = "status", required = false) Complaint.Status status, Authentication authentication) {
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
            return "employee/employee_complain";
        } catch (Exception e) {
            e.printStackTrace();
            return "error/500";
        }
    }

    @GetMapping("/fpt-dorm/employee/complain/execute/{id}")
    public String executeComplain(@PathVariable("id") int id, Model model) {
        var complain = complainRepository.getRequestByComplaintId(id);
        model.addAttribute("complainObj", complain);
        return "employee/execute_complain";
    }

    @PostMapping("/fpt-dorm/employee/complain/execute/{id}")
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
            notificationService.saveNotification(notification);
            try {
                List<Complaint> complainList = complainRepository.getAllRequest();
                model.addAttribute("complaintDTOList", complainList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "employee/employee_complain";
        } else {
            return "error/403";
        }
    }

    //Xu ly Student
    @GetMapping("/fpt-dorm/employee/student/all-student")
    public String showStudentList(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(defaultValue = "userId,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<User> userPage = userService.findAllStudent(pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("sort", sort);
        return "employee/student-manager/all_student";
    }

    @GetMapping("/fpt-dorm/employee/student/search")
    public String searchUserList(Model model,
                                 @RequestParam(value = "keyword", required = false) String keyword,
                                 @RequestParam(value = "category", required = false) String category,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @RequestParam(defaultValue = "userId,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<User> userPage;

        if (category == null || category.isEmpty()) {
            if (keyword == null || keyword.isEmpty()) {
                userPage = userService.findAllStudent(pageable);
            } else {
                userPage = userService.searchAllByStudent(keyword, pageable);
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int id = Integer.parseInt(keyword);
                        Optional<User> userOpt = repo.findById(id);
                        if (userOpt.isPresent()) {
                            userPage = new PageImpl<>(List.of(userOpt.get()), pageable, 1);
                        } else {
                            userPage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        userPage = Page.empty(pageable);
                    }
                    break;
                case "Name":
                    userPage = userService.searchByNameStudent(keyword, pageable);
                    break;
                case "Email":
                    userPage = userService.searchByEmailStudent(keyword, pageable);
                    break;
                case "Phone":
                    userPage = userService.searchByPhoneNumberStudent(keyword, pageable);
                    break;
                default:
                    userPage = Page.empty(pageable);
                    break;
            }
        }

        model.addAttribute("userPage", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "employee/student-manager/all_student";
    }

    @GetMapping("/fpt-dorm/employee/student/edit/id={id}")
    public String showEditPage(Model model, @PathVariable("id") int id) {
        try {
            if (repo.findById(id).isPresent()) {
                User user = repo.findById(id).get();

                model.addAttribute("user", user);

                UserDTO userDTO = new UserDTO();
                userDTO.setUsername(user.getUsername());
                userDTO.setEmail(user.getEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                userDTO.setGender(user.getGender());
                userDTO.setAddress(user.getAddress());
                userDTO.setBirthdate(user.getBirthdate());
                userDTO.setRoleUser(user.getRoleUser());
                userDTO.setCccdNumber(user.getCccdNumber());
                model.addAttribute("userDTO", userDTO);
            } else throw new Exception();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/employee/student/all-student";
        }
        return "employee/student-manager/edit_student";
    }

    @PostMapping("/fpt-dorm/employee/student/edit/save")
    public String saveEditUser(@ModelAttribute("userDTO") UserDTO userDTO,
                               @RequestParam("userId") int id,
                               Model model,
                               @RequestParam("avatar") MultipartFile[] avatars,
                               @RequestParam("frontface") MultipartFile[] frontCccdImages,
                               @RequestParam("backface") MultipartFile[] backCccdImages) {
        try {
            userService.updateUser(userDTO, id, avatars, frontCccdImages, backCccdImages);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating user. Please try again.");
            e.printStackTrace();
            return "employee/student-manager/edit_student";
        }
        return "redirect:/fpt-dorm/employee/student/edit/id=" + id + "?success";
    }

    @GetMapping("/fpt-dorm/employee/student/view/id={id}")
    public String showViewPage(Model model, @PathVariable("id") int id) {
        try {
            if (repo.findById(id).isPresent()) {
                User user = repo.findById(id).get();

                model.addAttribute("user", user);

                UserDTO userDTO = new UserDTO();
                userDTO.setUsername(user.getUsername());
                userDTO.setEmail(user.getEmail());
                userDTO.setPhoneNumber(user.getPhoneNumber());
                userDTO.setGender(user.getGender());
                userDTO.setAddress(user.getAddress());
                userDTO.setBirthdate(user.getBirthdate());
                userDTO.setRoleUser(user.getRoleUser());
                userDTO.setCccdNumber(user.getCccdNumber());
                model.addAttribute("userDTO", userDTO);
            } else throw new Exception();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/employee/student/all-student";
        }
        return "employee/student-manager/view_student";
    }

    @DeleteMapping("/fpt-dorm/employee/student/delete/id={id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") int id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fpt-dorm/employee/student/add")
    public String showCreateForm(Model model) {
        UserDTO userDTO = new UserDTO();
        model.addAttribute("userDTO", userDTO);
        return "employee/student-manager/add_student";
    }

    @PostMapping("/fpt-dorm/employee/student/add/submit")
    public String createUser(@ModelAttribute("userDTO") UserDTO userDTO,
                             Model model,
                             @RequestParam("avatar") MultipartFile[] avatars,
                             @RequestParam("frontface") MultipartFile[] frontCccdImages,
                             @RequestParam("backface") MultipartFile[] backCccdImages) {
        try {
            for (User user : repo.findAll()) {
                if (user.getEmail().equals(userDTO.getEmail())) {
                    model.addAttribute("errorMessage", "Email already exists");
                    return "employee/student-manager/add_student";
                }
            }
            userDTO.setRoleUser(User.Role.USER);
            userService.saveUser(userDTO, avatars, frontCccdImages, backCccdImages);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while creating user. Please try again.");
            e.printStackTrace();
            return "employee/student-manager/add_student";
        }
        return "redirect:/fpt-dorm/employee/student/add?success";
    }

    @GetMapping("/fpt-dorm/employee/usage-service")
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
        return "employee/employee_usageService";
    }

    @PostMapping("/fpt-dorm/employee/usage-service/{id}")
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
            return "employee/employee_usageService";
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
            usageService.setPaymentMethod(UsageService.PaymentMethod.Paypal);
            usageServiceService.saveUsageService(usageService);

            Notification notification = new Notification();
            notification.setUser(userRepository.searchUserById(user.getUserId()));
            notification.setContent("Your usage service bill is ready.");
            notification.setRead(false);
            notificationService.saveNotification(notification);
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
        return "employee/employee_usageService";
    }

    @GetMapping("/fpt-dorm/employee/notifications")
    public String notification(Model model, Authentication authentication) {
        User user = null;
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
        List<Notification> notifications = notificationRepository.getAllByUserId(user.getUserId());
        if (notifications.isEmpty()) {
            model.addAttribute("message", "No notification founded.");
        } else {
            model.addAttribute("notifications", notifications);
        }
        List<Dorm> dorms = dormRepository.findAll();
        model.addAttribute("dorms", dorms);
        return "employee/employee_notification";
    }


    @GetMapping("/fpt-dorm/employee/employee_Resident_History")
    public String employee_list_residentH() {
        return "redirect:/fpt-dorm/employee/Resident_History/list";
    }

}
