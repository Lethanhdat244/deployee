package com.accommodation_management_booking.controller;

import com.accommodation_management_booking.dto.NewDTO;
import com.accommodation_management_booking.entity.New;
import com.accommodation_management_booking.repository.NewRepository;
import com.accommodation_management_booking.service.NewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class NewAdminController {

    @Autowired
    private NewRepository newRepository;

    @Autowired
    private NewService newService;


    @GetMapping("/fpt-dorm/admin/add-news")
    public String showAddForm(Model model) {
        model.addAttribute("newDTO", new NewDTO());
        return "admin/add_news";
    }

    @PostMapping("/fpt-dorm/admin/add-news/save")
    public String saveNew(@ModelAttribute("newDTO") NewDTO newDTO,
                          Model model,
                          @RequestParam("images") MultipartFile images) {
        try {
            newService.addNews(newDTO, images);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            e.printStackTrace();
            return "admin/add_news";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while creating news. Please try again.");
            e.printStackTrace();
            return "admin/add_news";
        }
        return "redirect:/fpt-dorm/admin/add-news?success";
    }


    @GetMapping("/fpt-dorm/admin/view-news")
    public String viewNews(Model model,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "5") int size,
                           @RequestParam(required = false) String keyword) {
        Page<New> newsPage;
        if (keyword == null || keyword.isEmpty()) {
            newsPage = newService.getAllNewsByPage(page, size);
        } else {
            newsPage = newService.searchNewsByTitle(keyword, page, size);
            model.addAttribute("keyword", keyword);
        }
        model.addAttribute("newsPage", newsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        return "admin/view_news";
    }

    @GetMapping("/fpt-dorm/admin/news/detail/newsId={id}")
    public String newsDetail(Model model, @PathVariable("id") Long id) {
        New news = newService.getNewsById(id);
        model.addAttribute("news", news);
        return "admin/detail_new";
    }


    @DeleteMapping("fpt-dorm/admin/news/delete/id={id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fpt-dorm/admin/news/edit/id={id}")
    public String editNews(Model model, @PathVariable("id") Long id) {
        New news = newService.getNewsById(id);
        model.addAttribute("news", news);
        return "admin/edit_news";
    }

    @PostMapping("/fpt-dorm/admin/news/edit/save")
    public String editNews(@RequestParam("newsId") Long id,
                           @ModelAttribute("newDTO") NewDTO newDTO,
                           Model model,
                           @RequestParam("images") MultipartFile images) {
        try {
            newService.updateNews(id, newDTO, images);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating news. Please try again.");
            e.printStackTrace();
            return "admin/edit_news";
        }
        return "redirect:/fpt-dorm/admin/news/edit/id=" + id + "?success";
    }

}
