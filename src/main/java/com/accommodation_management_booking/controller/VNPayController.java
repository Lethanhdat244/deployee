package com.accommodation_management_booking.controller;


import com.accommodation_management_booking.config.VNPayConfig;
import com.accommodation_management_booking.entity.*;
import com.accommodation_management_booking.repository.*;
import com.accommodation_management_booking.service.EmailService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;


@Controller
public class VNPayController {


    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/fpt-dorm/user/booking/vnpay")
    public String VNPay(HttpServletRequest request,
                        @RequestParam("bed") Integer bedId,
                        @RequestParam("room") Integer roomId,
                        @RequestParam("checkin") LocalDate checkinDate,
                        @RequestParam("checkout") LocalDate checkoutDate,
                        @RequestParam("totalPrice") float totalPrice) throws UnsupportedEncodingException {

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
        bookingRepository.save(booking);

        bed.setIsAvailable(false);
        bedRepository.save(bed);

        // Store booking ID in session
        request.getSession().setAttribute("bookingId", booking.getBookingId());
        request.getSession().setAttribute("totalPrice", totalPrice);

        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        //long amount = 10000*100;
        String bankCode = "NCB";

        String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";

        String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf((int) totalPrice * 100));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrlSuccess);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        request.getSession().setAttribute("vnp_TxnRef", vnp_TxnRef);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;

        System.out.println(totalPrice);

        return "redirect:"+paymentUrl;
    }

    @GetMapping("/vnpay/cancel")
    public String cancelPay(HttpServletRequest request) {
        Integer bookingId = (Integer) request.getSession().getAttribute("bookingId");
        bookingRepository.deleteById(bookingId);
        return "cancel";
    }

    @GetMapping("/vnpay/success")
    public String successPay(HttpServletRequest request,
                             @RequestParam Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            Integer bookingId = (Integer) request.getSession().getAttribute("bookingId");
            String vnp_TxnRef = (String) request.getSession().getAttribute("vnp_TxnRef");
            if (bookingId == null) {
                return "redirect:/";
            }

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid booking ID"));

            Payment payment = new Payment();
            payment.setPaymentMethod(Payment.PaymentMethod.BankQRCode);
            payment.setPaymentDetail(vnp_TxnRef);
            payment.setPaymentDate(LocalDateTime.now());
            payment.setBooking(booking);
            paymentRepository.save(payment);

            booking.setAmountPaid(booking.getTotalPrice());
            bookingRepository.save(booking);

            // Send email
            String toEmail = booking.getUser().getEmail();
            String subject = "Payment Successful - Booking Confirmation";
            String body = "Dear " + booking.getUser().getUsername() + ",\n\nYour payment was successful." +
                    "\n Payment code: " + payment.getPaymentDetail() +
                    "\nTotal Price: " + booking.getTotalPrice() +
                    "\nAmount Paid: " + booking.getAmountPaid() +
                    "\nDate: " + payment.getPaymentDate() +
                    "\n\nThank you for your booking.";
            emailService.sendBill(toEmail, subject, body);

            return "success";
        } else {
            return "redirect:/vnpay/cancel";
        }
    }


}
