package com.english.learning.service;

import com.english.learning.entity.Sentence;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SentenceService {
    List<Sentence> getSentencesByLessonId(Long lessonId);
    List<Sentence> getPublishedSentencesByLessonId(Long lessonId);
    Optional<Sentence> getSentenceById(Long id);
    Optional<Sentence> getPublishedSentenceById(Long id);

    /**
     * Trả về Map<sentenceId, List<properNouns>> cho tất cả câu trong lesson.
     */
    Map<Long, List<String>> getProperNounHints(Long lessonId);
    Sentence createSentence(com.english.learning.dto.AdminSentenceRequest request);
    Sentence updateSentence(Long id, com.english.learning.dto.AdminSentenceRequest request);
    void softDeleteSentence(Long id);
    void restoreSentence(Long id);
    void hardDeleteSentence(Long id) throws Exception;
}
