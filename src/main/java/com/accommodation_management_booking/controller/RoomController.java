package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.entity.Bed;
import com.accommodation_management_booking.entity.Dorm;
import com.accommodation_management_booking.entity.Floor;
import com.accommodation_management_booking.entity.Room;
import com.accommodation_management_booking.service.RoomAllService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class RoomController {

    @Autowired
    private RoomAllService roomAllService;

    @GetMapping("/fpt-dorm/admin/dorms-by-gender")
    @ResponseBody
    public List<Dorm> getDormsByGender(@RequestParam("gender") Dorm.DormGender gender) {
        return roomAllService.getDormsByGender(gender);
    }

    @GetMapping("/fpt-dorm/admin/all-room")
    public String dormgender(Model model) {
        List<Dorm> dorms = roomAllService.getAllDorms();
        model.addAttribute("dorms", dorms);
        return "admin/admin_all_room";
    }







    @GetMapping("/fpt-dorm/admin/all-room/floor")
    @ResponseBody
    public List<Floor> getFloorsByDorm(@RequestParam Integer dormId) {
        return roomAllService.getFloorsByDormId(dormId);
    }




@GetMapping("/fpt-dorm/admin/all-room/room-list")
@ResponseBody
public Page<Room> getRoomsByFloor(@RequestParam Integer dormId, @RequestParam Integer floorNumber, @PageableDefault(size = 2) Pageable pageable) {
    return roomAllService.getRoomByDormIdAndFloorNumber(dormId, floorNumber, pageable);
}




    @GetMapping("/fpt-dorm/admin/room-detail")
    public String getRoomDetails(@RequestParam String roomNumber, Model model) {
        List<Bed> beds = roomAllService.getBedsByRoomNumber(roomNumber);
        if (beds != null && !beds.isEmpty()) {
            model.addAttribute("beds", beds);
        } else {
            model.addAttribute("message", "Currently, this room does not have any beds.");
        }
        return "/admin/room_detail";
    }




}
