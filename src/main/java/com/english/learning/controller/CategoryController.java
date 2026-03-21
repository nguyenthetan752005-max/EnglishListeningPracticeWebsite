package com.english.learning.controller;

import com.english.learning.entity.Category;
import com.english.learning.service.CategoryService;
import com.english.learning.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SectionService sectionService;

    @GetMapping("/category/{id}/sections")
    public String getSections(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại!"));
        model.addAttribute("category", category);
        model.addAttribute("sections", sectionService.getSectionsByCategoryId(id));
        return "category/sections";
    }
}
