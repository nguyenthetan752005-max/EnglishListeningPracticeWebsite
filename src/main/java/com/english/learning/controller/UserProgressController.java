package com.english.learning.controller;

import com.english.learning.entity.UserProgress;
import com.english.learning.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserProgressController {

    @Autowired
    private UserProgressService userProgressService;

    @PostMapping("/progress/update")
    @ResponseBody
    public UserProgress updateProgress(
            @RequestParam Long userId,
            @RequestParam Long lessonId,
            @RequestParam Integer completedSentences) {
        return userProgressService.updateProgress(userId, lessonId, completedSentences);
    }

    @PostMapping("/progress/complete")
    @ResponseBody
    public UserProgress completeLesson(
            @RequestParam Long userId,
            @RequestParam Long lessonId) {
        return userProgressService.completeLesson(userId, lessonId);
    }
}
