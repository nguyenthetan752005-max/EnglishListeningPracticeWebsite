package com.english.learning.dao;

import com.english.learning.model.Lesson;

import java.util.List;
import java.util.Optional;

public interface ILessonDAO {
    List<Lesson> findBySectionId(Long sectionId);
    Optional<Lesson> findById(Long id);
    Lesson save(Lesson lesson);
    void delete(Long id);
}
