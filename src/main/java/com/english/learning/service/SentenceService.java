package com.english.learning.service;

import com.english.learning.entity.Sentence;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SentenceService {
    List<Sentence> getSentencesByLessonId(Long lessonId);
    Optional<Sentence> getSentenceById(Long id);

    /**
     * Trả về Map<sentenceId, List<properNouns>> cho tất cả câu trong lesson.
     */
    Map<Long, List<String>> getProperNounHints(Long lessonId);
}
