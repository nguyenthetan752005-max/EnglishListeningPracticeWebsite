package com.english.learning.dao;

import com.english.learning.model.UserProgress;

import java.util.List;
import java.util.Optional;

public interface IUserProgressDAO {
    Optional<UserProgress> findByUserIdAndLessonId(Long userId, Long lessonId);
    List<UserProgress> findByUserId(Long userId);
    UserProgress save(UserProgress userProgress);
}
