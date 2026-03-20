package com.english.learning.service;

import com.english.learning.model.Lesson;

import java.util.List;
import java.util.Optional;

public interface ILessonService {
    List<Lesson> getLessonsBySectionId(Long sectionId);
    Optional<Lesson> getLessonById(Long id);
}