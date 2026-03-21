package com.english.learning.service;

import com.english.learning.entity.Lesson;

import java.util.List;
import java.util.Optional;

public interface LessonService {
    List<Lesson> getLessonsBySectionId(Long sectionId);
    Optional<Lesson> getLessonById(Long id);
}
