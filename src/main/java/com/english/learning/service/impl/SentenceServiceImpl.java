package com.english.learning.service.impl;

import com.english.learning.dao.ISentenceDAO;
import com.english.learning.model.Sentence;
import com.english.learning.service.ISentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SentenceServiceImpl implements ISentenceService {

    @Autowired
    private ISentenceDAO sentenceDAO;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        return sentenceDAO.findByLessonIdOrderByOrderIndex(lessonId);
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceDAO.findById(id);
    }
}
