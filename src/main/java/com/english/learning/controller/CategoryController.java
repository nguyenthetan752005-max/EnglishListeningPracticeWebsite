package com.english.learning.controller;

import com.english.learning.entity.Category;
import com.english.learning.entity.Lesson;
import com.english.learning.service.CategoryService;
import com.english.learning.service.SectionService;
import com.english.learning.service.LessonService;
import com.english.learning.service.UserProgressService;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.entity.Section;
import com.english.learning.enums.PracticeType;
import com.english.learning.enums.UserProgressStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Controller
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private UserProgressService userProgressService;

    @GetMapping("/exercises")
    public String showAllTopics(Model model, HttpSession session) {
        List<Category> categories = categoryService.getCategoriesByPracticeType(PracticeType.LISTENING);
        com.english.learning.entity.User user = (com.english.learning.entity.User) session.getAttribute("loggedInUser");
        
        Map<Long, UserProgressStatus> categoryStatuses = new HashMap<>();
        if (user != null) {
            for (Category cat : categories) {
                UserProgressStatus status = userProgressService.getCategoryStatus(user.getId(), cat.getId());
                if (status != null) {
                    categoryStatuses.put(cat.getId(), status);
                }
            }
        }
        
        model.addAttribute("categories", categories);
        model.addAttribute("categoryStatuses", categoryStatuses);
        return "exercises/categories-list";
    }

    @GetMapping("/category/{id}/sections")
    public String getSections(@PathVariable Long id, Model model, HttpSession session) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại!"));
        
        com.english.learning.entity.User user = (com.english.learning.entity.User) session.getAttribute("loggedInUser");
        
        List<Section> sections = sectionService.getSectionsByCategoryId(id);
        List<SectionWithLessonsDTO> sectionDtos = sections.stream()
                .map(sec -> {
                    List<Lesson> listeningLessons = lessonService.getLessonsBySectionId(sec.getId()).stream()
                            .filter(l -> l.getPracticeType() == PracticeType.LISTENING)
                            .toList();
                    
                    Map<Long, UserProgressStatus> lessonStatuses = new HashMap<>();
                    UserProgressStatus sectionStatus = null;
                    
                    if (user != null) {
                        for (Lesson l : listeningLessons) {
                            UserProgressStatus lStatus = userProgressService.getLessonStatus(user.getId(), l.getId());
                            if (lStatus != null) lessonStatuses.put(l.getId(), lStatus);
                        }
                        sectionStatus = userProgressService.getSectionStatus(user.getId(), sec.getId());
                    }
                    
                    return new SectionWithLessonsDTO(sec, listeningLessons, lessonStatuses, sectionStatus);
                })
                .filter(dto -> !dto.getLessons().isEmpty())
                .toList();
        List<String> expandedLevels = categoryService.getExpandedLevels(category.getLevelRange());
        
        model.addAttribute("category", category);
        model.addAttribute("sections", sectionDtos);
        model.addAttribute("levels", expandedLevels);
        
        return "exercises/sections";
    }
}
