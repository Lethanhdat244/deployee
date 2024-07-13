package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.entity.Bed;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MaintenanceStatusConverter implements AttributeConverter<Bed.MaintenanceStatus, String> {

    @Override
    public String convertToDatabaseColumn(Bed.MaintenanceStatus attribute) {
        if (attribute == null) {
            return null;
        }
        switch (attribute) {
            case Available:
                return "Available";
            case UnderMaintenance:
                return "Under Maintenance";
            default:
                throw new IllegalArgumentException("Unknown value: " + attribute);
        }
    }

    @Override
    public Bed.MaintenanceStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        switch (dbData) {
            case "Available":
                return Bed.MaintenanceStatus.Available;
            case "Under Maintenance":
                return Bed.MaintenanceStatus.UnderMaintenance;
            default:
                throw new IllegalArgumentException("Unknown database value: " + dbData);
        }
    }
}
