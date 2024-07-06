package com.accommodation_management_booking.service.impl;

import com.accommodation_management_booking.dto.NewDTO;
import com.accommodation_management_booking.entity.New;
import com.accommodation_management_booking.repository.NewRepository;
import com.accommodation_management_booking.service.CloudinaryService;
import com.accommodation_management_booking.service.NewService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NewServiceImpl implements NewService {

    @Autowired
    private NewRepository newsRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private Cloudinary cloudinary;

    @Override
    public List<NewDTO> getAllNews() {
        return newsRepository.findAll().stream()
                .map(news -> modelMapper.map(news, NewDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public New getNewsById(Long id) {
        return newsRepository.findById(id).orElse(null);
    }

    @Override
    public NewDTO saveNews(NewDTO newsDTO) {
        New news = modelMapper.map(newsDTO, New.class);
        return modelMapper.map(newsRepository.save(news), NewDTO.class);
    }

    @Override
    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    @Override
    public Page<New> getAllNewsByPage(int page, int size) {
        return newsRepository.findAllByOrderByNewsIdDesc(PageRequest.of(page, size));
    }
    @Override
    public Page<New> searchNewsByTitle(String keyword, int page, int size) {
        return newsRepository.findByTitleContainingIgnoreCaseOrderByNewsIdDesc(keyword, PageRequest.of(page, size));
    }

    @Override
    public void addNews(NewDTO news, MultipartFile multipartFile) {
        New newEntity = new New();
        newEntity.setTitle(news.getTitle());
        newEntity.setContent(news.getContent());

        if (!multipartFile.isEmpty()) {
            try {
                String imageUrl = uploadImage(multipartFile);
                newEntity.setImageUrl(imageUrl);
                System.out.println("Image uploaded successfully: " + imageUrl);  // Debug statement
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Image upload failed", e);
            }
        } else {
            System.out.println("No image to upload");  // Debug statement
        }

        try {
            newsRepository.save(newEntity);
            System.out.println("News saved successfully with title: " + newEntity.getTitle());  // Debug statement
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save news", e);
        }
    }

    @Override
    public void updateNews(Long id, NewDTO newsDTO, MultipartFile multipartFile) {
        New news = newsRepository.findById(id).orElse(null);
        if (news != null) {
            news.setTitle(newsDTO.getTitle());
            news.setContent(newsDTO.getContent());
            if (!multipartFile.isEmpty()) {
                try {
                    String imageUrl = uploadImage(multipartFile);
                    news.setImageUrl(imageUrl);
                    System.out.println("Image uploaded successfully: " + imageUrl);  // Debug statement
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Image upload failed", e);
                }
            } else {
                System.out.println("No image to upload");  // Debug statement
            }
            newsRepository.save(news);
        }
    }

    private String uploadImage(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url").toString();
    }
}
