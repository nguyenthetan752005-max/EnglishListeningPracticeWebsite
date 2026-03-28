package com.english.learning.service.impl;

import com.english.learning.repository.SentenceRepository;
import com.english.learning.entity.Sentence;
import com.english.learning.service.SentenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SentenceServiceImpl implements SentenceService {

    @Autowired
    private SentenceRepository sentenceRepository;

    @Autowired
    private com.english.learning.service.HintService hintService;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        for (Sentence sentence : sentences) {
            String text = sentence.getContent();
            text = text.replaceAll("<[^>]*>", "");
            text = text.replaceAll("\\[.*?\\]", "").trim();
            if (text.startsWith("- ")) {
                text = text.substring(2).trim();
            }
            text = text.replaceAll("\\s+", " ");
            sentence.setContent(text);
            sentence.setProperNouns(hintService.extractProperNouns(text));
        }
        return sentences;
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceRepository.findById(id);
    }

    @Override
    public Map<Long, List<String>> getProperNounHints(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        return hintService.getHintsMap(sentences);
    }
}
