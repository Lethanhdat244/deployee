package com.accommodation_management_booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bed")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bedId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private String bedName;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus maintenanceStatus = MaintenanceStatus.Available;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum MaintenanceStatus {
        Available, UnderMaintenance
    }
}