package com.english.learning.service.impl;

import com.english.learning.repository.SentenceRepository;
import com.english.learning.entity.Sentence;
import com.english.learning.service.SentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SentenceServiceImpl implements SentenceService {

    @Autowired
    private SentenceRepository sentenceRepository;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        return sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceRepository.findById(id);
    }
}
