package com.english.learning.controller;

import com.english.learning.model.Lesson;
import com.english.learning.model.Section;
import com.english.learning.service.ILessonService;
import com.english.learning.service.ISectionService;
import com.english.learning.service.ISentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class LessonController {

    @Autowired
    private ILessonService lessonService;

    @Autowired
    private ISectionService sectionService;

    @Autowired
    private ISentenceService sentenceService;

    @GetMapping("/section/{id}/lessons")
    public String getLessons(@PathVariable Long id, Model model) {
        Section section = sectionService.getSectionById(id)
                .orElseThrow(() -> new RuntimeException("Section không tồn tại!"));
        model.addAttribute("section", section);
        model.addAttribute("lessons", lessonService.getLessonsBySectionId(id));
        return "section/lessons";
    }

    @GetMapping("/lesson/{id}")
    public String getLesson(@PathVariable Long id, Model model) {
        Lesson lesson = lessonService.getLessonById(id)
                .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
        model.addAttribute("lesson", lesson);
        model.addAttribute("sentences", sentenceService.getSentencesByLessonId(id));
        return "lesson/dictation";
    }
}
