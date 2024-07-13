package com.accommodation_management_booking.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "dorm")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dorm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dormId;

    @Column(nullable = false, length = 10)
    private String dormName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DormGender dormGender;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();


    public enum DormGender {
        Male, Female, Other
    }
}
