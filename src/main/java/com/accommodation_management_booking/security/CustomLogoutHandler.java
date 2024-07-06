package com.accommodation_management_booking.security;

import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomLogoutHandler implements LogoutHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null) {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);

            if (user != null) {
                user.setActive(false); // Đặt isActive thành false khi đăng xuất
                userRepository.save(user);
                System.out.println("User " + email + " is logged out and isActive set to false.");
            }
        }
    }
}
