package com.english.learning.dao;

import com.english.learning.model.Sentence;

import java.util.List;
import java.util.Optional;

public interface ISentenceDAO {
    List<Sentence> findByLessonIdOrderByOrderIndex(Long lessonId);
    Optional<Sentence> findById(Long id);
    Sentence save(Sentence sentence);
    void delete(Long id);
}
