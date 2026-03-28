package com.english.learning.service;

import com.english.learning.entity.UserProgress;

import java.util.List;
import java.util.Optional;

public interface UserProgressService {
    Optional<UserProgress> getProgress(Long userId, Long lessonId);
    List<UserProgress> getProgressByUserId(Long userId);
    UserProgress updateProgress(Long userId, Long lessonId, Integer completedSentences);
    UserProgress completeLesson(Long userId, Long lessonId);
}
