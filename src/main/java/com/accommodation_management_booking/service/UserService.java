package com.accommodation_management_booking.service;

import com.accommodation_management_booking.dto.UserDTO;
import com.accommodation_management_booking.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void saveUser(UserDTO userDTO, MultipartFile[] avatars, MultipartFile[] frontCccdImages, MultipartFile[] backCccdImages);

    void completeUserProfile(UserDTO userDTO, MultipartFile avatar, MultipartFile frontCccdImage, MultipartFile backCccdImage);

    void processForgotPassword(String email);

    void resetPassword(String token, String newPassword);

    boolean changePassword(String currentPassword, String newPassword);

    void updateUser(UserDTO userDTO, int id, MultipartFile[] avatars, MultipartFile[] frontCccdImages, MultipartFile[] backCccdImages);

    void deleteUser(int id);

    Page<User> findAllStudent(Pageable pageable);
    Page<User> searchAllByStudent(String keyword, Pageable pageable);
    Page<User> searchByNameStudent(String name, Pageable pageable);
    Page<User> searchByEmailStudent(String email, Pageable pageable);
    Page<User> searchByPhoneNumberStudent(String phoneNumber, Pageable pageable);

    Page<User> findAllEmployee(Pageable pageable);
    Page<User> searchAllByEmployee(String keyword, Pageable pageable);
    Page<User> searchByNameEmployee(String name, Pageable pageable);
    Page<User> searchByEmailEmployee(String email, Pageable pageable);
    Page<User> searchByPhoneNumberEmployee(String phoneNumber, Pageable pageable);

}
