package com.accommodation_management_booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int equipmentId;

    private int roomId;

    @Column(length = 100)
    private String name;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private EquipCondition equipCondition;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum EquipCondition {
        Good, Fair, Poor
    }
}