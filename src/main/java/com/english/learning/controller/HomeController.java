package com.english.learning.controller;

import com.english.learning.enums.SlideshowPosition;
import com.english.learning.service.CategoryService;
import com.english.learning.service.SlideshowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SlideshowService slideshowService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("slideshows", slideshowService.getActiveSlideshowsByPosition(SlideshowPosition.HOME));
        return "home";
    }
}
