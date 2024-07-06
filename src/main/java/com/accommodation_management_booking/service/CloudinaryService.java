package com.accommodation_management_booking.service;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public Map<String, Object> upload(MultipartFile[] files)  {
        try {
            Map<String, Object> uploadResult = new HashMap<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    Map<String, Object> data = this.cloudinary.uploader().upload(file.getBytes(), Map.of());
                    uploadResult.put(file.getOriginalFilename(), data);
                }
            }
            return uploadResult;
        } catch (IOException io) {
            throw new RuntimeException("Image upload fail");
        }
    }

}
