package com.english.learning.service.progress;

import com.english.learning.entity.UserProgress;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserProgressQueryService {
    Optional<UserProgress> getProgress(Long userId, Long sentenceId);

    List<UserProgress> getProgressByUserId(Long userId);

    List<UserProgress> getProgressByUserIdAndLessonId(Long userId, Long lessonId);

    Map<Long, String> getUserProgressMapAsStrings(Long userId, Long lessonId);
}

