package com.english.learning.service;

import com.english.learning.model.Sentence;

import java.util.List;
import java.util.Optional;

public interface ISentenceService {
    List<Sentence> getSentencesByLessonId(Long lessonId);
    Optional<Sentence> getSentenceById(Long id);
}
