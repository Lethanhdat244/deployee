package com.accommodation_management_booking.service;

import com.accommodation_management_booking.dto.ServiceDTO;

import com.accommodation_management_booking.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    public List<ServiceDTO> getAllServiceDTOs() {
        List<com.accommodation_management_booking.entity.Service> services = serviceRepository.findAll();
        return services.stream().map(service -> {
            ServiceDTO dto = new ServiceDTO();
            dto.setServiceId(service.getServiceId());
            dto.setName(service.getName());
            dto.setPrice(service.getPrice());
            dto.setDescription(service.getDescription());
//            dto.setCreatedAt(service.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    public ServiceDTO getServiceDetailById(int serviceId) {

        com.accommodation_management_booking.entity.Service service = serviceRepository.findById(serviceId).orElse(null);
        if (service != null) {
            ServiceDTO serviceDTO = new ServiceDTO();
            serviceDTO.setServiceId(service.getServiceId());
            serviceDTO.setName(service.getName());
            serviceDTO.setPrice(service.getPrice());
            serviceDTO.setDescription(service.getDescription());
//            serviceDTO.setCreatedAt(service.getCreatedAt());
            return serviceDTO;
        }
        return null;
    }
}
