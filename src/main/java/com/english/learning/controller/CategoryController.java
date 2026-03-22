package com.english.learning.controller;

import com.english.learning.entity.Category;
import com.english.learning.service.CategoryService;
import com.english.learning.service.SectionService;
import com.english.learning.service.LessonService;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.entity.Section;
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

    @Autowired
    private LessonService lessonService;

    @GetMapping("/exercises")
    public String showAllTopics(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "exercises/categories-list";
    }

    @GetMapping("/category/{id}/sections")
    public String getSections(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại!"));
        
        java.util.List<Section> sections = sectionService.getSectionsByCategoryId(id);
        java.util.List<SectionWithLessonsDTO> sectionDtos = sections.stream()
                .map(sec -> new SectionWithLessonsDTO(sec, lessonService.getLessonsBySectionId(sec.getId())))
                .toList();
        // Expand category.levelRange (e.g. "A1-C1") into individual levels
        java.util.List<String> ALL_LEVELS = java.util.List.of("A1", "A2", "B1", "B2", "C1", "C2");
        java.util.List<String> expandedLevels = new java.util.ArrayList<>();
        
        String range = category.getLevelRange();
        if (range != null && range.contains("-")) {
            String[] parts = range.split("-");
            String start = parts[0].trim().toUpperCase();
            String end = parts[1].trim().toUpperCase();
            int startIdx = ALL_LEVELS.indexOf(start);
            int endIdx = ALL_LEVELS.indexOf(end);
            if (startIdx >= 0 && endIdx >= 0 && startIdx <= endIdx) {
                expandedLevels = ALL_LEVELS.subList(startIdx, endIdx + 1);
            }
        } else if (range != null && !range.trim().isEmpty()) {
            // Single level like "OET" or "A1"
            expandedLevels = java.util.List.of(range.trim().toUpperCase());
        }
                
        model.addAttribute("category", category);
        model.addAttribute("sections", sectionDtos);
        model.addAttribute("levels", expandedLevels);
        
        return "exercises/sections";
    }
}
