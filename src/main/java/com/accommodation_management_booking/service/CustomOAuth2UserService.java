package com.accommodation_management_booking.service;

import com.accommodation_management_booking.entity.User;
import com.accommodation_management_booking.repository.UserRepository;
import com.accommodation_management_booking.security.CustomOAuth2User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        CustomOAuth2User customOAuth2User = new CustomOAuth2User(oAuth2User);
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email);

        if (user == null) {
            // First-time login, create a new user and save to the repository
            user = new User();
            user.setEmail(customOAuth2User.getEmail());
            user.setUsername(customOAuth2User.getName());
            user.setRoleUser(User.Role.USER);
            user.setProfileComplete(false); // mark as profile not complete
            userRepository.save(user);
        }

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRoleUser().name()));

        return new DefaultOAuth2User(authorities, oAuth2User.getAttributes(), "email");
    }
}
