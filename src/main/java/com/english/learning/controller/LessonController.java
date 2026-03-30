package com.english.learning.controller;

import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.service.LessonService;
import com.english.learning.service.SectionService;
import com.english.learning.service.SentenceService;
import com.english.learning.service.UserProgressService;
import com.english.learning.service.HintService;
import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.enums.PracticeType;
import com.english.learning.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.english.learning.entity.Sentence;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;

@Controller
public class LessonController {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private SectionService sectionService;

    @Autowired
    private SentenceService sentenceService;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private HintService hintService;

    @GetMapping("/section/{id}/lessons")
    public String getLessons(@PathVariable Long id, Model model) {
        Section section = sectionService.getSectionById(id)
                .orElseThrow(() -> new RuntimeException("Section không tồn tại!"));

        // Chỉ lấy các bài học có practiceType là LISTENING cho trang Dictation
        List<Lesson> listeningLessons = lessonService.getLessonsBySectionId(id).stream()
                .filter(l -> l.getSection().getCategory()
                        .getPracticeType() == com.english.learning.enums.PracticeType.LISTENING)
                .toList();

        model.addAttribute("section", section);
        model.addAttribute("lessons", listeningLessons);
        return "section/lessons";
    }

    @GetMapping("/lesson/{id}")
    public String getLesson(@PathVariable Long id,
            @RequestParam(required = false) Integer sentenceIndex,
            Model model, HttpSession session) {
        Optional<Lesson> lessonOpt = lessonService.getLessonById(id);
        if (lessonOpt.isEmpty()) {
            return "redirect:/";
        }

        Lesson lesson = lessonOpt.get();
        List<Sentence> sentences = sentenceService.getSentencesByLessonId(id);

        if (sentences == null || sentences.isEmpty()) {
            return "redirect:/";
        }

        LessonNavigationDTO navigation = lessonService.getLessonNavigation(lesson, PracticeType.LISTENING);
        com.english.learning.dto.LessonDTO nextLesson = navigation.getNextLesson();
        boolean isLastLessonInSection = navigation.isLastLessonInSection();
        Section section = lesson.getSection();

        model.addAttribute("lesson", lesson);
        model.addAttribute("sentences", sentences);
        model.addAttribute("hintsMap", hintService.getHintsMap(sentences));

        if (session.getAttribute("loggedInUser") != null) {
            User user = (User) session.getAttribute("loggedInUser");
            model.addAttribute("userProgressMap", userProgressService.getUserProgressMapAsStrings(user.getId(), id));
        } else {
            model.addAttribute("userProgressMap", new HashMap<>());
        }

        model.addAttribute("category", section != null ? section.getCategory() : null);
        model.addAttribute("section", section);
        model.addAttribute("nextLesson", nextLesson);
        model.addAttribute("isLastLessonInSection", isLastLessonInSection);
        model.addAttribute("initialSentenceIndex", sentenceIndex != null ? sentenceIndex : 0);

        return "lesson/dictation";
    }
}
