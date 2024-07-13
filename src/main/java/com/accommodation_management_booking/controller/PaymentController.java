package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.config.PaypalPaymentIntent;
import com.accommodation_management_booking.config.PaypalPaymentMethod;
import com.accommodation_management_booking.config.VNPayConfig;
import com.accommodation_management_booking.dto.PaymentTransactionDTO;
import com.accommodation_management_booking.entity.Bed;
import com.accommodation_management_booking.entity.Booking;
import com.accommodation_management_booking.entity.Room;
import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.*;
import com.accommodation_management_booking.service.EmailService;
import com.accommodation_management_booking.service.PaymentService;
import com.accommodation_management_booking.service.PaypalService;
//import com.accommodation_management_booking.service.VNPayService;
import com.accommodation_management_booking.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.accommodation_management_booking.config.VNPayConfig.hmacSHA512;

@Controller
@AllArgsConstructor
public class PaymentController {

    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @GetMapping("/fpt-dorm/employee/all-payment")
    public String showPaymentList(Model model,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "5") int size,
                                  @RequestParam(defaultValue = "userId,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<User> userPage = paymentService.getPayments(pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("sort", sort);
        return "employee/payment/all_payment";
    }

    @GetMapping("/fpt-dorm/employee/all-payment/search")
    public String searchPaymentList(Model model,
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
                userPage = paymentService.getPayments(pageable);
            } else {
                userPage = paymentService.searchAll(keyword, pageable);
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int id = Integer.parseInt(keyword);
                        userPage = paymentService.searchByUser(id, pageable);
                    } catch (NumberFormatException e) {
                        userPage = Page.empty(pageable);
                    }
                    break;
                case "Name":
                    userPage = paymentService.searchByName(keyword, pageable);
                    break;
                case "Email":
                    userPage = paymentService.searchByMail(keyword, pageable);
                    break;
                case "Phone":
                    userPage = paymentService.searchByPhone(keyword, pageable);
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
        return "employee/payment/all_payment";
    }

    @GetMapping("/fpt-dorm/employee/all-payment/student/id={id}")
    public String showStudentPaymentList(Model model,
                                         @PathVariable("id") int id,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "5") int size,
                                         @RequestParam(defaultValue = "paymentId,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<PaymentTransactionDTO> paymentPage;
        Pageable pageable;
        List<String> bookingSortFields = List.of("totalPrice");
        if (bookingSortFields.contains(sortParams[0])) {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByUserWithBookingSort(id, pageable);
        } else {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByUserWithPaymentSort(id, pageable);
        }
        model.addAttribute("userId", id);
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("sort", sort);
        return "employee/payment/student_payment_list";
    }

    @GetMapping("/fpt-dorm/employee/all-payment/student/search")
    public String searchStudentPaymentListEmployee(Model model,
                                                   @RequestParam(value = "userId", required = false) int userId,
                                                   @RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "category", required = false) String category,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size,
                                                   @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<PaymentTransactionDTO> paymentPage;

        System.out.println("paymentDate: " + keyword);

        if (category == null || category.isEmpty()) {
            if (keyword == null || keyword.isEmpty()) {
                paymentPage = Page.empty(pageable);
            } else {
                paymentPage = paymentService.searchByUserWithPaymentSort(userId, pageable);
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int paymentId = Integer.parseInt(keyword);
                        if (paymentService.findByPaymentId(paymentId) != null) {
                            paymentPage = paymentService.findByPaymentIdWithPage(paymentId, pageable);
                        } else {
                            paymentPage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                case "Date":
                    try {
                        List<DateTimeFormatter> formatters = Arrays.asList(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        );
                        LocalDate paymentDate = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                paymentDate = LocalDate.parse(keyword, formatter);
                                break;
                            } catch (DateTimeParseException _) {
                            }
                        }
                        if (paymentDate == null) {
                            paymentDate = LocalDate.parse(keyword);
                        }
                        paymentPage = paymentService.findByPaymentDateWithPage(paymentDate, userId, pageable);
                    } catch (DateTimeParseException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                default:
                    paymentPage = Page.empty(pageable);
                    break;
            }
        }
        model.addAttribute("userId", userId);
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "employee/payment/student_payment_list";
    }

    @GetMapping("/fpt-dorm/employee/all-payment/student/payment/id={id}")
    public String showPaymentDetail(Model model, @PathVariable("id") int id) {
        try {
            PaymentTransactionDTO paymentTransactionDTO = paymentService.findByPaymentId(id);
            if (paymentTransactionDTO != null) {
                model.addAttribute("payment", paymentTransactionDTO);
                model.addAttribute("user", userRepository.findByEmail(paymentTransactionDTO.getEmail()));
            } else {
                throw new Exception("Payment not found");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/employee/all-payment";
        }
        return "employee/payment/payment_detail";
    }

    @GetMapping("/fpt-dorm/employee/payment-request")
    public String showPaymentRequestList(Model model,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "5") int size,
                                         @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<PaymentTransactionDTO> paymentPage;
        Pageable pageable;
        List<String> bookingSortFields = List.of("totalPrice");
        if (bookingSortFields.contains(sortParams[0])) {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByStatusWithBookingSort(Booking.Status.Pending, pageable);
        } else {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByStatusWithPaymentSort(Booking.Status.Pending, pageable);
        }
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("sort", sort);
        return "employee/payment/payment_request";
    }

    @GetMapping("/fpt-dorm/employee/payment-request/search")
    public String searchStudentPaymentRequestEmployee(Model model,
                                                      @RequestParam(value = "keyword", required = false) String keyword,
                                                      @RequestParam(value = "category", required = false) String category,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "5") int size,
                                                      @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<PaymentTransactionDTO> paymentPage;

        System.out.println("paymentDate: " + keyword);

        if (category == null || category.isEmpty()) {
            if (keyword == null || keyword.isEmpty()) {
                paymentPage = Page.empty(pageable);
            } else {
                model.addAttribute("paymentPage", Page.empty(pageable));
                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedCategory", category);
                model.addAttribute("sort", sort);
                return "employee/payment/payment_request";
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int paymentId = Integer.parseInt(keyword);
                        if (paymentService.findByPaymentId(paymentId) != null) {
                            paymentPage = paymentService.findPaymentRequestByPaymentId(paymentId, pageable);
                        } else {
                            paymentPage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                case "Date":
                    try {
                        List<DateTimeFormatter> formatters = Arrays.asList(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        );
                        LocalDate paymentDate = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                paymentDate = LocalDate.parse(keyword, formatter);
                                break;
                            } catch (DateTimeParseException ex) {
                            }
                        }
                        if (paymentDate == null) {
                            paymentDate = LocalDate.parse(keyword);
                        }
                        paymentPage = paymentService.findPaymentRequestByPaymentDate(paymentDate, pageable);
                    } catch (DateTimeParseException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                default:
                    paymentPage = Page.empty(pageable);
                    break;
            }
        }
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "employee/payment/payment_request";
    }

    @GetMapping("/fpt-dorm/employee/payment-request/id={id}")
    public String showPaymentRequestDetailEmployee(Model model, @PathVariable("id") int id) {
        try {
            PaymentTransactionDTO paymentTransactionDTO = paymentService.findByPaymentId(id);
            if (paymentTransactionDTO != null) {
                model.addAttribute("payment", paymentTransactionDTO);
                model.addAttribute("user", userRepository.findByEmail(paymentTransactionDTO.getEmail()));
            } else {
                throw new Exception("Payment not found");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/employee/all-payment";
        }
        return "employee/payment/payment_request_detail";
    }

//    @GetMapping("/fpt-dorm/employee/payment-request/cancel/id={id}")
//    public ResponseEntity<String> cancelBooking(@PathVariable("id") int id) {
//        Optional<Booking> optionalBooking = bookingRepository.findById(id);
//
//        if (optionalBooking.isPresent()) {
//            Booking booking = optionalBooking.get();
//            booking.setStatus(Booking.Status.Canceled);
//            bookingRepository.save(booking);
//            return ResponseEntity.ok("Canceled successfully");
//        } else {
//            return ResponseEntity.status(404).body("Booking not found");
//        }
//    }

    @PostMapping("/fpt-dorm/employee/payment-request/confirm")
    public ResponseEntity<String> confirmPayment(@RequestBody Booking request) {
        int bookingId = request.getBookingId();
        float refundAmount = request.getRefundAmount();
        LocalDate refundDate = request.getRefundDate();

        // Simulate processing (replace with actual logic)
        // Example: Update database, send notifications, etc.
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Refund Amount: " + refundAmount);
        System.out.println("Refund Date: " + refundDate);

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setRefundAmount(refundAmount);
            booking.setRefundDate(refundDate);
            booking.setStatus(Booking.Status.Confirmed);
            bookingRepository.save(booking);
            return ResponseEntity.ok("Payment confirmed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
    }

    //User
    @GetMapping("/fpt-dorm/user/payment")
    public String showPaymentUser(Model model, Authentication authentication,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "3") int size,
                                  @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String email;
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            model.addAttribute("email", "Unknown");
            return "user/payment";
        }
        if (email != null) {
            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            Pageable pendingPageable = PageRequest.of(0, 20, Sort.by(direction, sortParams[0]));

            Page<PaymentTransactionDTO> pendingPaymentPage = paymentService.findPendingPaymentsByUserEmail(email, pendingPageable);
            Page<PaymentTransactionDTO> paymentPage = paymentService.findPaymentsByUserEmail(email, pageable);

            model.addAttribute("pendingPaymentPage", pendingPaymentPage);
            model.addAttribute("paymentPage", paymentPage);
            model.addAttribute("sort", sort);
            model.addAttribute("email", email);
        }

        return "user/payment";
    }

    @GetMapping("/fpt-dorm/user/payment/id={id}")
    public String showPaymentDetailUser(Model model, Authentication authentication, @PathVariable("id") int id) {
        String email;
        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            email = oauth2User.getAttribute("email");
        } else if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            model.addAttribute("email", "Unknown");
            return "user/payment";
        }
        try {
            PaymentTransactionDTO paymentTransactionDTO = paymentService.findByPaymentId(id);
            if (paymentTransactionDTO != null && paymentTransactionDTO.getEmail().equals(email)) {
                model.addAttribute("payment", paymentTransactionDTO);
            } else {
                throw new Exception("Payment not found");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/user/payment";
        }
        return "user/student_payment_detail";
    }

    //Admin
    @GetMapping("/fpt-dorm/admin/all-payment")
    public String showPaymentListAdmin(Model model,
                                       @RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "5") int size,
                                       @RequestParam(defaultValue = "userId,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<User> userPage = paymentService.getPayments(pageable);
        model.addAttribute("userPage", userPage);
        model.addAttribute("sort", sort);
        return "admin/payment/all_payment";
    }

    @GetMapping("/fpt-dorm/admin/all-payment/search")
    public String searchPaymentListAdmin(Model model,
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
                userPage = paymentService.getPayments(pageable);
            } else {
                userPage = paymentService.searchAll(keyword, pageable);
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int id = Integer.parseInt(keyword);
                        userPage = paymentService.searchByUser(id, pageable);
                    } catch (NumberFormatException e) {
                        userPage = Page.empty(pageable);
                    }
                    break;
                case "Name":
                    userPage = paymentService.searchByName(keyword, pageable);
                    break;
                case "Email":
                    userPage = paymentService.searchByMail(keyword, pageable);
                    break;
                case "Phone":
                    userPage = paymentService.searchByPhone(keyword, pageable);
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
        return "admin/payment/all_payment";
    }

    @GetMapping("/fpt-dorm/admin/all-payment/student/id={id}")
    public String showStudentPaymentListAdmin(Model model,
                                              @PathVariable("id") int id,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int size,
                                              @RequestParam(defaultValue = "paymentId,asc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<PaymentTransactionDTO> paymentPage;
        Pageable pageable;
        List<String> bookingSortFields = List.of("totalPrice");
        if (bookingSortFields.contains(sortParams[0])) {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByUserWithBookingSort(id, pageable);
        } else {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByUserWithPaymentSort(id, pageable);
        }
        model.addAttribute("userId", id);
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("sort", sort);
        return "admin/payment/student_payment_list";
    }

    @GetMapping("/fpt-dorm/admin/all-payment/student/search")
    public String searchStudentPaymentListAdmin(Model model,
                                                @RequestParam(value = "userId", required = false) int userId,
                                                @RequestParam(value = "keyword", required = false) String keyword,
                                                @RequestParam(value = "category", required = false) String category,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "5") int size,
                                                @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<PaymentTransactionDTO> paymentPage;

        if (category == null || category.isEmpty()) {
            if (keyword == null || keyword.isEmpty()) {
                paymentPage = Page.empty(pageable);
            } else {
                paymentPage = paymentService.searchByUserWithPaymentSort(userId, pageable);
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int paymentId = Integer.parseInt(keyword);
                        if (paymentService.findByPaymentId(paymentId) != null) {
                            paymentPage = paymentService.findByPaymentIdWithPage(paymentId, pageable);
                        } else {
                            paymentPage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                case "Date":
                    try {
                        List<DateTimeFormatter> formatters = Arrays.asList(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        );
                        LocalDate paymentDate = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                paymentDate = LocalDate.parse(keyword, formatter);
                                break;
                            } catch (DateTimeParseException _) {
                            }
                        }
                        if (paymentDate == null) {
                            paymentDate = LocalDate.parse(keyword);
                        }
                        paymentPage = paymentService.findByPaymentDateWithPage(paymentDate, userId, pageable);
                    } catch (DateTimeParseException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                default:
                    paymentPage = Page.empty(pageable);
                    break;
            }
        }
        model.addAttribute("userId", userId);
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "admin/payment/student_payment_list";
    }

    @GetMapping("/fpt-dorm/admin/all-payment/student/payment/id={id}")
    public String showPaymentDetailAdmin(Model model, @PathVariable("id") int id) {
        try {
            PaymentTransactionDTO paymentTransactionDTO = paymentService.findByPaymentId(id);
            if (paymentTransactionDTO != null) {
                model.addAttribute("payment", paymentTransactionDTO);
                model.addAttribute("user", userRepository.findByEmail(paymentTransactionDTO.getEmail()));
            } else {
                throw new Exception("Payment not found");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/admin/all-payment";
        }
        return "admin/payment/payment_detail";
    }

    @GetMapping("/fpt-dorm/admin/payment-request")
    public String showPaymentRequestListAdmin(Model model,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int size,
                                              @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Page<PaymentTransactionDTO> paymentPage;
        Pageable pageable;
        List<String> bookingSortFields = List.of("totalPrice");
        if (bookingSortFields.contains(sortParams[0])) {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByStatusWithBookingSort(Booking.Status.Pending, pageable);
        } else {
            pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
            paymentPage = paymentService.searchByStatusWithPaymentSort(Booking.Status.Pending, pageable);
        }
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("sort", sort);
        return "admin/payment/payment_request";
    }

    @GetMapping("/fpt-dorm/admin/payment-request/search")
    public String searchStudentPaymentRequestAdmin(Model model,
                                                   @RequestParam(value = "keyword", required = false) String keyword,
                                                   @RequestParam(value = "category", required = false) String category,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "5") int size,
                                                   @RequestParam(defaultValue = "paymentDate,desc") String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        Page<PaymentTransactionDTO> paymentPage;

        System.out.println("paymentDate: " + keyword);

        if (category == null || category.isEmpty()) {
            if (keyword == null || keyword.isEmpty()) {
                paymentPage = Page.empty(pageable);
            } else {
                model.addAttribute("paymentPage", Page.empty(pageable));
                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedCategory", category);
                model.addAttribute("sort", sort);
                return "admin/payment/payment_request";
//                return "redirect:/fpt-dorm/admin/payment-request";
            }
        } else {
            switch (category) {
                case "ID":
                    try {
                        int paymentId = Integer.parseInt(keyword);
                        if (paymentService.findByPaymentId(paymentId) != null) {
                            paymentPage = paymentService.findPaymentRequestByPaymentId(paymentId, pageable);
                        } else {
                            paymentPage = Page.empty(pageable);
                        }
                    } catch (NumberFormatException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                case "Date":
                    try {
                        List<DateTimeFormatter> formatters = Arrays.asList(
                                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        );
                        LocalDate paymentDate = null;
                        for (DateTimeFormatter formatter : formatters) {
                            try {
                                paymentDate = LocalDate.parse(keyword, formatter);
                                break;
                            } catch (DateTimeParseException ex) {
                            }
                        }
                        if (paymentDate == null) {
                            paymentDate = LocalDate.parse(keyword);
                        }
                        paymentPage = paymentService.findPaymentRequestByPaymentDate(paymentDate, pageable);
                    } catch (DateTimeParseException e) {
                        paymentPage = Page.empty(pageable);
                    }
                    break;
                default:
                    paymentPage = Page.empty(pageable);
                    break;
            }
        }
        model.addAttribute("paymentPage", paymentPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("sort", sort);
        return "admin/payment/payment_request";
    }

    @GetMapping("/fpt-dorm/admin/payment-request/id={id}")
    public String showPaymentRequestDetailAdmin(Model model, @PathVariable("id") int id) {
        try {
            PaymentTransactionDTO paymentTransactionDTO = paymentService.findByPaymentId(id);
            if (paymentTransactionDTO != null) {
                model.addAttribute("payment", paymentTransactionDTO);
                model.addAttribute("user", userRepository.findByEmail(paymentTransactionDTO.getEmail()));
            } else {
                throw new Exception("Payment not found");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return "redirect:/fpt-dorm/admin/all-payment";
        }
        return "admin/payment/payment_request_detail";
    }

    @GetMapping("/fpt-dorm/admin/payment-request/cancel/id={id}")
    public ResponseEntity<String> cancelBookingAdmin(@PathVariable("id") int id) throws IOException {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);

        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            System.out.println("Booking found: " + booking);
            booking.setStatus(Booking.Status.Canceled);
            bookingRepository.save(booking);

            if (booking.getAmountPaid() > 0) {
                Optional<com.accommodation_management_booking.entity.Payment> optionalPayment = paymentRepository.findByBooking(booking);
                if(optionalPayment.isPresent()){
                    if (optionalPayment.get().getPaymentMethod() == com.accommodation_management_booking.entity.Payment.PaymentMethod.PayPal) {
                        System.out.println("Paypal");
                        com.accommodation_management_booking.entity.Payment payment = optionalPayment.get();
                        System.out.println("Payment found: " + payment);
                        try{
                            // Get the current exchange rate from VND to USD
                            float exchangeRate = getExchangeRateVNDToUSD();
                            if (exchangeRate == 0) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve exchange rate.");
                            }

                            // Convert the amount paid from VND to USD
                            float amountPaidInUSD = booking.getAmountPaid() * exchangeRate;
                            float refundAmount = amountPaidInUSD /exchangeRate;
                            // Logging for debugging
                            System.out.println("Amount paid in VND: " + booking.getAmountPaid());
                            System.out.println("Exchange rate VND to USD: " + exchangeRate);
                            System.out.println("Amount to be refunded in USD: " + amountPaidInUSD);
                            paypalService.refundPayment(payment.getPaymentDetail(), amountPaidInUSD);
                            booking.setRefundAmount(refundAmount);
                            booking.setRefundDate(LocalDate.now());
                            bookingRepository.save(booking);


                            // Update the bed status
                            Integer bedId = booking.getBed().getBedId();
                            Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
                            bed.setIsAvailable(true);
                            bedRepository.save(bed);

                            // Send email
                            String toEmail = booking.getUser().getEmail(); // Assuming you have a getEmail method in your Customer entity
                            String subject = "Refund successful";
                            String body = "Dear " + booking.getUser().getUsername() + ",\n\nYour payment has been refunded successfully"+
                                    "\n Please check your account: " + payment.getPaymentDetail() +
                                    "\nRefundAmount: " + booking.getRefundAmount() +
                                    "\nDate: " + booking.getRefundDate() +
                                    "\nDue to PayPal's refund policy, your refund may vary from the amount you paid!"+
                                    "\nAny questions or complaints please contact us:"+
                                    "\nAddress: Education and Training Area - Hoa Lac High-Tech Park - Km29 Thang Long Avenue, Thach That, City. HN" +
                                    "\nPhone: (024) 7300.1866 / (024) 7300.5588" +
                                    "\n\nThank you for using our service.";
                            emailService.sendBill(toEmail, subject, body);

                            return ResponseEntity.ok("Canceled successfully");
                        } catch (PayPalRESTException e) {
                            e.printStackTrace();
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Refund failed: " + e.getMessage());
                        }
                    } else if (optionalPayment.get().getPaymentMethod() == com.accommodation_management_booking.entity.Payment.PaymentMethod.BankQRCode) {
                        System.out.println("VNPay");
                        com.accommodation_management_booking.entity.Payment payment = optionalPayment.get();
                        System.out.println("Payment found: " + payment);
                        //Command: refund
                        String vnp_RequestId = VNPayConfig.getRandomNumber(8);
                        String vnp_Version = "2.1.0";
                        String vnp_Command = "refund";
                        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
                        String vnp_TransactionType = "refund";
                        String vnp_TxnRef = payment.getPaymentDetail();
                        long amount = Math.round(booking.getAmountPaid());
                        String vnp_Amount = String.valueOf(amount);
                        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
                        String vnp_TransactionNo = payment.getPaymentDetail();
                        String vnp_TransactionDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        String vnp_CreateBy = "admin";

                        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                        String vnp_CreateDate = formatter.format(cld.getTime());

                        String vnp_IpAddr = "127.0.0.1";;

                        JsonObject  vnp_Params = new JsonObject ();

                        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
                        vnp_Params.addProperty("vnp_Version", vnp_Version);
                        vnp_Params.addProperty("vnp_Command", vnp_Command);
                        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
                        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
                        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
                        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
                        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);

                        vnp_Params.addProperty("vnp_TransactionNo", vnp_TransactionNo);

                        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransactionDate);
                        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
                        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
                        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

                        String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                                vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

                        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hash_Data.toString());

                        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

                        // Log each parameter
                        System.out.println("vnp_RequestId: " + vnp_RequestId);
                        System.out.println("vnp_Version: " + vnp_Version);
                        System.out.println("vnp_Command: " + vnp_Command);
                        System.out.println("vnp_TmnCode: " + vnp_TmnCode);
                        System.out.println("vnp_TransactionType: " + vnp_TransactionType);
                        System.out.println("vnp_TxnRef: " + vnp_TxnRef);
                        System.out.println("vnp_Amount: " + vnp_Amount);
                        System.out.println("vnp_OrderInfo: " + vnp_OrderInfo);
                        System.out.println("vnp_TransactionDate: " + vnp_TransactionDate);
                        System.out.println("vnp_CreateBy: " + vnp_CreateBy);
                        System.out.println("vnp_CreateDate: " + vnp_CreateDate);
                        System.out.println("vnp_IpAddr: " + vnp_IpAddr);
                        System.out.println("vnp_SecureHash: " + vnp_SecureHash);

                        URL url = new URL (VNPayConfig.vnp_ApiUrl);
                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setDoOutput(true);
                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes(vnp_Params.toString());
                        wr.flush();
                        wr.close();
                        int responseCode = con.getResponseCode();
                        System.out.println("nSending 'POST' request to URL : " + url);
                        System.out.println("Post Data : " + vnp_Params);
                        System.out.println("Response Code : " + responseCode);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String output;
                        StringBuffer response = new StringBuffer();
                        while ((output = in.readLine()) != null) {
                            response.append(output);
                        }
                        in.close();
                        System.out.println(response.toString());

                        booking.setRefundAmount(Float.parseFloat(vnp_Amount));
                        booking.setRefundDate(LocalDate.now());
                        bookingRepository.save(booking);

                        // Update the bed status
                        Integer bedId = booking.getBed().getBedId();
                        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
                        bed.setIsAvailable(true);
                        bedRepository.save(bed);

                        // Send email
                        String toEmail = booking.getUser().getEmail(); // Assuming you have a getEmail method in your Customer entity
                        String subject = "Refund successful";
                        String body = "Dear " + booking.getUser().getUsername() + ",\n\nYour payment has been refunded successfully"+
                                "\n Please check your account: " + payment.getPaymentDetail() +
                                "\nRefundAmount: " + booking.getRefundAmount() +
                                "\nDate: " + booking.getRefundDate() +
                                "\nDue to VNPay's refund policy, your refund may vary from the amount you paid!"+
                                "\nAny questions or complaints please contact us:"+
                                "\nAddress: Education and Training Area - Hoa Lac High-Tech Park - Km29 Thang Long Avenue, Thach That, City. HN" +
                                "\nPhone: (024) 7300.1866 / (024) 7300.5588" +
                                "\n\nThank you for using our service.";
                        emailService.sendBill(toEmail, subject, body);

                        return ResponseEntity.ok("Canceled successfully");
                    }

                }
            }else {
                System.out.println("No payment found for this booking");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No payment found for this booking");
            }
            return ResponseEntity.ok("Canceled successfully, no payment to refund");
        }else {
            System.out.println("Booking not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
    }

    @PostMapping("/fpt-dorm/admin/payment-request/confirm")
    public ResponseEntity<String> confirmPaymentAdmin(@RequestBody Booking request) {
        int bookingId = request.getBookingId();
        float refundAmount = request.getRefundAmount();
        LocalDate refundDate = request.getRefundDate();

        // Simulate processing (replace with actual logic)
        // Example: Update database, send notifications, etc.
        System.out.println("Booking ID: " + bookingId);
        System.out.println("Refund Amount: " + refundAmount);
        System.out.println("Refund Date: " + refundDate);

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            booking.setRefundAmount(refundAmount);
            booking.setRefundDate(refundDate);
            booking.setStatus(Booking.Status.Confirmed);
            bookingRepository.save(booking);
            return ResponseEntity.ok("Payment confirmed successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }
    }

    public static final String URL_PAYPAL_SUCCESS = "pay/success";
    public static final String URL_PAYPAL_CANCEL = "pay/cancel";

    //private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private PaypalService paypalService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

//    @Autowired
//    private VNPayService vnPayService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    private Integer getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                User user = userRepository.findByEmail(userDetails.getUsername());
                return user.getUserId(); // Assuming userId is the field name
            } else if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                String email = oauth2User.getAttribute("email");
                User user = userRepository.findByEmail(email);
                return user.getUserId();
            }
        }
        throw new IllegalStateException("User not found in context");
    }

    @PostMapping("/fpt-dorm/user/booking/pay")
    public String pay(HttpServletRequest request,
                      @RequestParam("bed") Integer bedId,
                      @RequestParam("room") Integer roomId,
                      @RequestParam("checkin") LocalDate checkinDate,
                      @RequestParam("checkout") LocalDate checkoutDate,
                      @RequestParam("totalPrice") float totalPrice,
                      @RequestParam("totalPriceUSD") float price) {

        Integer userId = getLoggedInUserId();
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
        Room room = roomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Invalid room ID"));
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Booking booking = new Booking();
        booking.setBed(bed);
        booking.setRoom(room);
        booking.setUser(user);
        booking.setStartDate(checkinDate);
        booking.setEndDate(checkoutDate);
        booking.setTotalPrice(totalPrice);
//        booking.setAmountPaid(totalPrice);
        bookingRepository.save(booking);

//        bed.setIsAvailable(false);
//        bedRepository.save(bed);

        // Store booking ID in session
        request.getSession().setAttribute("bookingId", booking.getBookingId());
        request.getSession().setAttribute("bedId", bed.getBedId());

        String cancelUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = Utils.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        try{
            Payment payment = paypalService.createPayment(
                    price,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "Payment for booking",
                    cancelUrl,
                    successUrl);
            for (Links link : ((com.paypal.api.payments.Payment) payment).getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return "redirect:" + link.getHref();
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @GetMapping(URL_PAYPAL_CANCEL)
    public String cancelPay(HttpServletRequest request) {
        // Store booking ID in session
        Integer bookingId = (Integer) request.getSession().getAttribute("bookingId");
        bookingRepository.deleteById(bookingId);
        Integer bedId = (Integer) request.getSession().getAttribute("bedId");
        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
        bed.setIsAvailable(true);
        bedRepository.save(bed);
        return "cancel";
    }

    @GetMapping(URL_PAYPAL_SUCCESS)
    public String successPay(@RequestParam("paymentId") String paymentId,
                             @RequestParam("PayerID") String payerId,
                             HttpServletRequest request) {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                Integer bookingId = (Integer) request.getSession().getAttribute("bookingId");
                com.accommodation_management_booking.entity.Payment payment1 = new com.accommodation_management_booking.entity.Payment();
                payment1.setPaymentMethod(com.accommodation_management_booking.entity.Payment.PaymentMethod.PayPal);
                payment1.setPaymentDetail(paymentId);
                payment1.setPaymentDate(LocalDateTime.now());
                // Set booking ID in payment
                Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));
                payment1.setBooking(booking);
                paymentRepository.save(payment1);

                Integer bedId = (Integer) request.getSession().getAttribute("bedId");
                Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
                bed.setIsAvailable(false);
                bedRepository.save(bed);
                booking.setAmountPaid(payment1.getBooking().getTotalPrice());
                bookingRepository.save(booking);


                // Send email
                String toEmail = booking.getUser().getEmail(); // Assuming you have a getEmail method in your Customer entity
                String subject = "Payment Successful - Booking Confirmation";
                String body = "Dear " + booking.getUser().getUsername() + ",\n\nYour payment was successful."+
                        "\n Payment code: " + payment1.getPaymentDetail() +
                        "\nTotal Price: " + booking.getTotalPrice() +
                        "\nAmount Paid: " + booking.getAmountPaid() +
                        "\nDate: " + payment1.getPaymentDate() +
                        "\n\nThank you for your booking.";
                emailService.sendBill(toEmail, subject, body);

                return "success";
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
        }
        bookingRepository.delete(bookingRepository.findById((Integer) request.getSession().getAttribute("bookingId")).get());
        return "redirect:/";
    }


    @GetMapping("/fpt-dorm/employee/payment-request/cancel/id={id}")
    public ResponseEntity<String> cancelBooking(@PathVariable("id") int id) throws IOException {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);

        if (optionalBooking.isPresent()) {
            Booking booking = optionalBooking.get();
            System.out.println("Booking found: " + booking);
            booking.setStatus(Booking.Status.Canceled);
            bookingRepository.save(booking);

            if (booking.getAmountPaid() > 0) {
                Optional<com.accommodation_management_booking.entity.Payment> optionalPayment = paymentRepository.findByBooking(booking);
                if(optionalPayment.isPresent()){
                    if (optionalPayment.get().getPaymentMethod() == com.accommodation_management_booking.entity.Payment.PaymentMethod.PayPal) {
                        System.out.println("Paypal");
                        com.accommodation_management_booking.entity.Payment payment = optionalPayment.get();
                        System.out.println("Payment found: " + payment);
                        try{
                            // Get the current exchange rate from VND to USD
                            float exchangeRate = getExchangeRateVNDToUSD();
                            if (exchangeRate == 0) {
                                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve exchange rate.");
                            }

                            // Convert the amount paid from VND to USD
                            float amountPaidInUSD = booking.getAmountPaid() * exchangeRate;
                            float refundAmount = amountPaidInUSD /exchangeRate;
                            // Logging for debugging
                            System.out.println("Amount paid in VND: " + booking.getAmountPaid());
                            System.out.println("Exchange rate VND to USD: " + exchangeRate);
                            System.out.println("Amount to be refunded in USD: " + amountPaidInUSD);
                            paypalService.refundPayment(payment.getPaymentDetail(), amountPaidInUSD);
                            booking.setRefundAmount(refundAmount);
                            booking.setRefundDate(LocalDate.now());
                            bookingRepository.save(booking);


                            // Update the bed status
                            Integer bedId = booking.getBed().getBedId();
                            Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
                            bed.setIsAvailable(true);
                            bedRepository.save(bed);

                            // Send email
                            String toEmail = booking.getUser().getEmail(); // Assuming you have a getEmail method in your Customer entity
                            String subject = "Refund successful";
                            String body = "Dear " + booking.getUser().getUsername() + ",\n\nYour payment has been refunded successfully"+
                                    "\n Please check your account: " + payment.getPaymentDetail() +
                                    "\nRefundAmount: " + booking.getRefundAmount() +
                                    "\nDate: " + booking.getRefundDate() +
                                    "\nDue to PayPal's refund policy, your refund may vary from the amount you paid!"+
                                    "\nAny questions or complaints please contact us:"+
                                    "\nAddress: Education and Training Area - Hoa Lac High-Tech Park - Km29 Thang Long Avenue, Thach That, City. HN" +
                                    "\nPhone: (024) 7300.1866 / (024) 7300.5588" +
                                    "\n\nThank you for using our service.";
                            emailService.sendBill(toEmail, subject, body);

                            return ResponseEntity.ok("Canceled successfully");
                        } catch (PayPalRESTException e) {
                            e.printStackTrace();
                            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Refund failed: " + e.getMessage());
                        }
                    } else if (optionalPayment.get().getPaymentMethod() == com.accommodation_management_booking.entity.Payment.PaymentMethod.BankQRCode) {
                        System.out.println("VNPay");
                        com.accommodation_management_booking.entity.Payment payment = optionalPayment.get();
                        System.out.println("Payment found: " + payment);
                        //Command: refund
                        String vnp_RequestId = VNPayConfig.getRandomNumber(8);
                        String vnp_Version = "2.1.0";
                        String vnp_Command = "refund";
                        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;
                        String vnp_TransactionType = "refund";
                        String vnp_TxnRef = payment.getPaymentDetail();
                        long amount = Math.round(booking.getAmountPaid());
                        String vnp_Amount = String.valueOf(amount);
                        String vnp_OrderInfo = "Hoan tien GD OrderId:" + vnp_TxnRef;
                        String vnp_TransactionNo = payment.getPaymentDetail();
                        String vnp_TransactionDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                        String vnp_CreateBy = "admin";

                        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                        String vnp_CreateDate = formatter.format(cld.getTime());

                        String vnp_IpAddr = "127.0.0.1";;

                        JsonObject  vnp_Params = new JsonObject ();

                        vnp_Params.addProperty("vnp_RequestId", vnp_RequestId);
                        vnp_Params.addProperty("vnp_Version", vnp_Version);
                        vnp_Params.addProperty("vnp_Command", vnp_Command);
                        vnp_Params.addProperty("vnp_TmnCode", vnp_TmnCode);
                        vnp_Params.addProperty("vnp_TransactionType", vnp_TransactionType);
                        vnp_Params.addProperty("vnp_TxnRef", vnp_TxnRef);
                        vnp_Params.addProperty("vnp_Amount", vnp_Amount);
                        vnp_Params.addProperty("vnp_OrderInfo", vnp_OrderInfo);

                        vnp_Params.addProperty("vnp_TransactionNo", vnp_TransactionNo);

                        vnp_Params.addProperty("vnp_TransactionDate", vnp_TransactionDate);
                        vnp_Params.addProperty("vnp_CreateBy", vnp_CreateBy);
                        vnp_Params.addProperty("vnp_CreateDate", vnp_CreateDate);
                        vnp_Params.addProperty("vnp_IpAddr", vnp_IpAddr);

                        String hash_Data= String.join("|", vnp_RequestId, vnp_Version, vnp_Command, vnp_TmnCode,
                                vnp_TransactionType, vnp_TxnRef, vnp_Amount, vnp_TransactionNo, vnp_TransactionDate,
                                vnp_CreateBy, vnp_CreateDate, vnp_IpAddr, vnp_OrderInfo);

                        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hash_Data.toString());

                        vnp_Params.addProperty("vnp_SecureHash", vnp_SecureHash);

                        // Log each parameter
                        System.out.println("vnp_RequestId: " + vnp_RequestId);
                        System.out.println("vnp_Version: " + vnp_Version);
                        System.out.println("vnp_Command: " + vnp_Command);
                        System.out.println("vnp_TmnCode: " + vnp_TmnCode);
                        System.out.println("vnp_TransactionType: " + vnp_TransactionType);
                        System.out.println("vnp_TxnRef: " + vnp_TxnRef);
                        System.out.println("vnp_Amount: " + vnp_Amount);
                        System.out.println("vnp_OrderInfo: " + vnp_OrderInfo);
                        System.out.println("vnp_TransactionDate: " + vnp_TransactionDate);
                        System.out.println("vnp_CreateBy: " + vnp_CreateBy);
                        System.out.println("vnp_CreateDate: " + vnp_CreateDate);
                        System.out.println("vnp_IpAddr: " + vnp_IpAddr);
                        System.out.println("vnp_SecureHash: " + vnp_SecureHash);

                        URL url = new URL (VNPayConfig.vnp_ApiUrl);
                        HttpURLConnection con = (HttpURLConnection)url.openConnection();
                        con.setRequestMethod("POST");
                        con.setRequestProperty("Content-Type", "application/json");
                        con.setDoOutput(true);
                        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                        wr.writeBytes(vnp_Params.toString());
                        wr.flush();
                        wr.close();
                        int responseCode = con.getResponseCode();
                        System.out.println("nSending 'POST' request to URL : " + url);
                        System.out.println("Post Data : " + vnp_Params);
                        System.out.println("Response Code : " + responseCode);
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String output;
                        StringBuffer response = new StringBuffer();
                        while ((output = in.readLine()) != null) {
                            response.append(output);
                        }
                        in.close();
                        System.out.println(response.toString());

                        booking.setRefundAmount(Float.parseFloat(vnp_Amount));
                        booking.setRefundDate(LocalDate.now());
                        bookingRepository.save(booking);

                        // Update the bed status
                        Integer bedId = booking.getBed().getBedId();
                        Bed bed = bedRepository.findById(bedId).orElseThrow(() -> new IllegalArgumentException("Invalid bed ID"));
                        bed.setIsAvailable(true);
                        bedRepository.save(bed);

                        // Send email
                        String toEmail = booking.getUser().getEmail(); // Assuming you have a getEmail method in your Customer entity
                        String subject = "Refund successful";
                        String body = "Dear " + booking.getUser().getUsername() + ",\n\nYour payment has been refunded successfully"+
                                "\n Please check your account: " + payment.getPaymentDetail() +
                                "\nRefundAmount: " + booking.getRefundAmount() +
                                "\nDate: " + booking.getRefundDate() +
                                "\nDue to VNPay's refund policy, your refund may vary from the amount you paid!"+
                                "\nAny questions or complaints please contact us:"+
                                "\nAddress: Education and Training Area - Hoa Lac High-Tech Park - Km29 Thang Long Avenue, Thach That, City. HN" +
                                "\nPhone: (024) 7300.1866 / (024) 7300.5588" +
                                "\n\nThank you for using our service.";
                        emailService.sendBill(toEmail, subject, body);

                        return ResponseEntity.ok("Canceled successfully");
                    }

                }
            }else {
                System.out.println("No payment found for this booking");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No payment found for this booking");
            }
            return ResponseEntity.ok("Canceled successfully, no payment to refund");
        }else {
            System.out.println("Booking not found with id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Booking not found");
        }

    }

    private float getExchangeRateVNDToUSD() {
        // Example implementation using a hypothetical external service
        // Replace with actual API call to get the current exchange rate
        try {
            // Example: Using a service like ExchangeRate-API
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.exchangerate-api.com/v4/latest/VND";
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("rates")) {
                Map<String, Double> rates = (Map<String, Double>) responseBody.get("rates");
                return rates.get("USD").floatValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @GetMapping("/payments")
    @ResponseBody
    public List<com.accommodation_management_booking.entity.Payment> getPayments(@RequestParam com.accommodation_management_booking.entity.Payment.PaymentMethod method) {
        System.out.println("method: " + method);
        return paypalService.getPaymentsByMethod(method);
    }

}
