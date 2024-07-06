package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.dto.ServiceDTO;
import com.accommodation_management_booking.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/fpt-dorm/admin")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping("/service_list")
    public String serviceTable(Model model) {
        List<ServiceDTO> serviceDTOs = serviceService.getAllServiceDTOs();
        model.addAttribute("serviceList", serviceDTOs);
        return "admin_list_service";
    }

    @GetMapping("/serviceDetail/{id}")
    public String serviceDetail(@PathVariable int id, Model model) {
        ServiceDTO serviceDTO = serviceService.getServiceDetailById(id);
        model.addAttribute("service", serviceDTO);
        return "admin_service_detail";
    }
}
