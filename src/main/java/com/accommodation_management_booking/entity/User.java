package com.accommodation_management_booking.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Role roleUser;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthdate;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String address;

    @Email
    @Column(length = 100, unique = true, nullable = false)
    private String email;

    @Column(length = 12)
    private String cccdNumber;

    @Column(length = 255)
    private String avatar;

    @Column(length = 255)
    private String frontCccdImage;

    @Column(length = 255)
    private String backCccdImage;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive = true;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isProfileComplete = false;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum Gender {
        Male, Female, Other
    }
    public enum Role {
        ADMIN, MANAGER, EMPLOYEE, USER
    }

}