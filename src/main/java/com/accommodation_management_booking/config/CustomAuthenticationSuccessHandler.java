package com.accommodation_management_booking.config;

import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);

        if (user != null) {
            user.setActive(true);
            userRepository.save(user);
        }

        String redirectURL = request.getContextPath();

        if (user != null && !user.isProfileComplete()) {
            redirectURL = "/fpt-dorm/profile/complete";
        } else {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (authority.getAuthority().equals("ROLE_ADMIN")) {
                    redirectURL = "/fpt-dorm/admin/home";
                    break;
                } else if (authority.getAuthority().equals("ROLE_EMPLOYEE")) {
                    redirectURL = "/fpt-dorm/employee/home";
                    break;
                } else if (authority.getAuthority().equals("ROLE_USER")) {
                    redirectURL = "/fpt-dorm/user/news";
                    break;
                }
            }
        }

        response.sendRedirect(redirectURL);
    }

}
