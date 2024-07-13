package com.accommodation_management_booking.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfiguration {

    @Bean
    public Cloudinary cloudinaryConfig(){
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "djuwlqgvb");
        config.put("api_key", "259649773865917");
        config.put("api_secret", "mU3eWPUkQ1QFbWaXgbLB8Pfxrd8");
        config.put("secure", "true");

        return new Cloudinary(config);
    }
}