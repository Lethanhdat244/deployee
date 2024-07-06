package com.accommodation_management_booking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RoomController {

    @GetMapping("fpt-dorm/home/room")
    public String room(){
        return "room";
    }



}
