package com.english.learning.controller;

import com.english.learning.entity.Category;
import com.english.learning.entity.Lesson;
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
import com.english.learning.service.UserProgressService;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.enums.PracticeType;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.HashMap;

@Controller
public class SpeakingExerciseController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private LessonService lessonService;

    @Autowired
    private UserProgressService userProgressService;

    @GetMapping("/speaking-exercises")
    public String showAllTopics(Model model, HttpSession session) {
        java.util.List<Category> categories = categoryService.getCategoriesByPracticeType(com.english.learning.enums.PracticeType.SPEAKING);
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
        return "speaking/categories-list";
    }

    @GetMapping("/speaking/category/{id}/sections")
    public String getSections(@PathVariable Long id, Model model, HttpSession session) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại!"));

        com.english.learning.entity.User user = (com.english.learning.entity.User) session.getAttribute("loggedInUser");

        java.util.List<Section> sections = sectionService.getSectionsByCategoryId(id);
        java.util.List<SectionWithLessonsDTO> sectionDtos = sections.stream()
                .map(sec -> {
                    java.util.List<Lesson> speakingLessons = lessonService.getLessonsBySectionId(sec.getId()).stream()
                            .filter(l -> l.getSection().getCategory().getPracticeType() == PracticeType.SPEAKING)
                            .toList();
                    
                    Map<Long, UserProgressStatus> lessonStatuses = new HashMap<>();
                    UserProgressStatus sectionStatus = null;
                    
                    if (user != null) {
                        for (Lesson l : speakingLessons) {
                            UserProgressStatus lStatus = userProgressService.getLessonStatus(user.getId(), l.getId());
                            if (lStatus != null) lessonStatuses.put(l.getId(), lStatus);
                        }
                        sectionStatus = userProgressService.getSectionStatus(user.getId(), sec.getId());
                    }
                    
                    return new SectionWithLessonsDTO(sec, speakingLessons, lessonStatuses, sectionStatus);
                })
                .filter(dto -> !dto.getLessons().isEmpty())
                .toList();

        // Expand category.levelRange into individual levels
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
            expandedLevels = java.util.List.of(range.trim().toUpperCase());
        }

        model.addAttribute("category", category);
        model.addAttribute("sections", sectionDtos);
        model.addAttribute("levels", expandedLevels);

        return "speaking/sections";
    }

    @Autowired
    private com.english.learning.service.SentenceService sentenceService;

    @Autowired
    private com.english.learning.service.HintService hintService;

    @GetMapping("/speaking/lesson/{id}")
    public String getSpeakingPractice(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id)
                .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
        
        // Lấy danh sách câu
        java.util.List<com.english.learning.entity.Sentence> sentences = sentenceService.getSentencesByLessonId(id);

        // Dùng HintService để lấy map
        java.util.Map<Long, java.util.List<String>> hintsMap = hintService.getHintsMap(sentences);

        model.addAttribute("lesson", lesson);
        model.addAttribute("sentences", sentences);
        model.addAttribute("hintsMap", hintsMap); // Truyền map này xuống Thymeleaf
        model.addAttribute("category", lesson.getSection().getCategory());
        return "speaking/speaking-practice";
    }
}
