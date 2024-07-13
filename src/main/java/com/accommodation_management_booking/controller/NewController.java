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
public class NewController {

    @Autowired
    private NewRepository newRepository;

    @Autowired
    private NewService newService;


    @GetMapping("/fpt-dorm/employee/add-news")
    public String showAddForm(Model model) {
        model.addAttribute("newDTO", new NewDTO());
        return "employee/add_news";
    }

    @PostMapping("/fpt-dorm/employee/add-news/save")
    public String saveNew(@ModelAttribute("newDTO") NewDTO newDTO,
                          Model model,
                          @RequestParam("images") MultipartFile images) {
        try {
            newService.addNews(newDTO, images);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            e.printStackTrace();
            return "employee/add_news";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while creating news. Please try again.");
            e.printStackTrace();
            return "employee/add_news";
        }
        return "redirect:/fpt-dorm/employee/add-news?success";
    }


    @GetMapping("fpt-dorm/user/news")
    public String newfeed(Model model, Authentication authentication) {
        List<New> newList=newRepository.findTop10ByOrderByNewsIdDesc();
        model.addAttribute("newDTOList",newList);
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String email = oauth2User.getAttribute("email");
            model.addAttribute("email", email);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            model.addAttribute("email", userDetails.getUsername());
        } else {
            // Handle cases where the authentication is not OAuth2
            model.addAttribute("email", "Unknown");
        }
        return "new";
    }

    @GetMapping("/fpt-dorm/employee/view-news")
    public String viewNews(Model model,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "5") int size,
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
        return "employee/view_news";
    }

    @GetMapping("/fpt-dorm/employee/news/detail/newsId={id}")
    public String newsDetail(Model model, @PathVariable("id") Long id) {
        New news = newService.getNewsById(id);
        model.addAttribute("news", news);
        return "employee/detail_new";
    }

    @GetMapping("/fpt-dorm/user/news/detail/id={id}")
    public String newsDetailUser(Model model, @PathVariable("id") Long id) {
        New news = newService.getNewsById(id);
        model.addAttribute("newsUser", news);
        return "employee/detail_news_user";
    }

    @DeleteMapping("fpt-dorm/employee/news/delete/id={id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fpt-dorm/employee/news/edit/id={id}")
    public String editNews(Model model, @PathVariable("id") Long id) {
        New news = newService.getNewsById(id);
        model.addAttribute("news", news);
        return "employee/edit_news";
    }

    @PostMapping("/fpt-dorm/employee/news/edit/save")
    public String editNews(@RequestParam("newsId") Long id,
                           @ModelAttribute("newDTO") NewDTO newDTO,
                           Model model,
                           @RequestParam("images") MultipartFile images) {
        try {
            newService.updateNews(id, newDTO, images);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "An error occurred while updating news. Please try again.");
            e.printStackTrace();
            return "employee/edit_news";
        }
        return "redirect:/fpt-dorm/employee/news/edit/id=" + id + "?success";
    }

}
