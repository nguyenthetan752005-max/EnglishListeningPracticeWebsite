package com.english.learning.service;

import com.english.learning.entity.Lesson;

import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.enums.PracticeType;

import java.util.List;
import java.util.Optional;

public interface LessonService {
    List<Lesson> getLessonsBySectionId(Long sectionId);
    Optional<Lesson> getLessonById(Long id);
    LessonNavigationDTO getLessonNavigation(Lesson currentLesson, PracticeType practiceType);
}
