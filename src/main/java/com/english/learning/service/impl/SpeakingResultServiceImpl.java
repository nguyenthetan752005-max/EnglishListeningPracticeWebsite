package com.english.learning.service.impl;

import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.service.SpeakingResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeakingResultServiceImpl implements SpeakingResultService {

    @Autowired
    private SpeakingResultRepository speakingResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SentenceRepository sentenceRepository;

    @Override
    public SpeakingResult saveSpeakingResult(Long userId, Long sentenceId, Double accuracy, String recognizedText, String userAudioUrl) {
        SpeakingResult result = new SpeakingResult();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));

        result.setUser(user);
        result.setSentence(sentence);
        result.setAccuracy(accuracy);
        result.setRecognizedText(recognizedText);
        result.setUserAudioUrl(userAudioUrl);

        return speakingResultRepository.save(result);
    }

    @Override
    public List<SpeakingResult> getResultsByUserId(Long userId) {
        return speakingResultRepository.findByUser_Id(userId);
    }
}
