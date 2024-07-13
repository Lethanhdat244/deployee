package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.config.GenderMapper;
import com.accommodation_management_booking.entity.*;
import com.accommodation_management_booking.repository.*;
import com.accommodation_management_booking.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
public class BookingController {

    @Autowired
    private DormRepository dormRepository;

    @Autowired
    private FloorRepository floorRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BedRepository bedRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaypalService paypalService;

    @Autowired
    private APIContext apiContext;

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

    @GetMapping("fpt-dorm/user/booking")
    public String booking() {
        return "user/booking_type_room";
    }

    @PostMapping("fpt-dorm/user/booking/select")
    public String selectRoomType(@RequestParam("roomType") String roomType, Model model) {
        Integer userId = getLoggedInUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        Dorm.DormGender dormGender = GenderMapper.map(user.getGender());

        List<Dorm> dorms = dormRepository.findByDormGender(dormGender);
        model.addAttribute("roomType", roomType);
        model.addAttribute("dorms", dorms);
        model.addAttribute("userId", userId);
        return "user/booking_details";
    }

    @GetMapping("fpt-dorm/user/booking/floors")
    @ResponseBody
    public List<Floor> getFloors(@RequestParam("dormId") Integer dormId) {
        return floorRepository.findByDormDormId(dormId);
    }

    @GetMapping("fpt-dorm/user/booking/rooms")
    @ResponseBody
    public List<Room> getRooms(@RequestParam("floorId") Integer floorId, @RequestParam("capacity") Integer capacity) {
        return roomRepository.findByFloorFloorIdAndCapacity(floorId, capacity);
    }

    @GetMapping("fpt-dorm/user/booking/roomsByFloor")
    @ResponseBody
    public List<Room> getRooms(@RequestParam("floorId") Integer floorId) {
        return roomRepository.findByFloorFloorId(floorId);
    }

    @GetMapping("fpt-dorm/user/booking/beds")
    @ResponseBody
    public List<Bed> getBeds(@RequestParam("roomId") Integer roomId) {
        return bedRepository.findByRoomRoomIdAndIsAvailableTrueAndMaintenanceStatus(roomId, Bed.MaintenanceStatus.Available);
    }

    @PostMapping("fpt-dorm/user/booking/confirm")
    public String confirmBooking(@RequestParam("bed") Integer bedId,
                                 @RequestParam("room") Integer roomId,
                                 @RequestParam("checkin") LocalDate checkinDate,
                                 @RequestParam("checkout") LocalDate checkoutDate,
                                 @RequestParam("totalPrice") Float totalPrice) {

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

        return "user/booking_confirmation";
    }

}
