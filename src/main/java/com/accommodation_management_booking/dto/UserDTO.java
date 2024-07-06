package com.accommodation_management_booking.dto;

import com.accommodation_management_booking.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private int userId;

    @NotNull
    @Size(max = 50)
    private String username;

    @Size(max = 255)
    private String password;

    @NotNull
    private User.Role roleUser;

    private User.Gender gender;

    private LocalDate birthdate;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 255)
    private String address;

    @NotNull
    @Email
    @Size(max = 100)
    private String email;

    @Size(max = 12)
    private String cccdNumber;

    private MultipartFile avatar;

    private MultipartFile frontCccdImage;

    private MultipartFile backCccdImage;

    private boolean isActive=false;

}