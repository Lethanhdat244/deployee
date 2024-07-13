package com.accommodation_management_booking.service;
import com.accommodation_management_booking.dto.NewDTO;
import com.accommodation_management_booking.entity.New;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface NewService {
    List<NewDTO> getAllNews();
    New getNewsById(Long id);
    NewDTO saveNews(NewDTO newsDTO);
    void deleteNews(Long id);
    Page<New> getAllNewsByPage(int page, int size);
    Page<New> searchNewsByTitle(String keyword, int page, int size);
    void addNews(NewDTO news, MultipartFile multipartFile);
    void updateNews(Long id, NewDTO newsDTO, MultipartFile multipartFile);
}
