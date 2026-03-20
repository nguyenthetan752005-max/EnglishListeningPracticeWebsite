package com.english.learning.service.impl;

import com.english.learning.dao.ISentenceDAO;
import com.english.learning.dao.ISpeakingResultDAO;
import com.english.learning.dao.IUserDAO;
import com.english.learning.model.Sentence;
import com.english.learning.model.SpeakingResult;
import com.english.learning.model.User;
import com.english.learning.service.ISpeakingResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpeakingResultServiceImpl implements ISpeakingResultService {

    @Autowired
    private ISpeakingResultDAO speakingResultDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private ISentenceDAO sentenceDAO;

    @Override
    public SpeakingResult saveSpeakingResult(Long userId, Long sentenceId, Double accuracy, String recognizedText, String userAudioUrl) {
        SpeakingResult result = new SpeakingResult();

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        Sentence sentence = sentenceDAO.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));

        result.setUser(user);
        result.setSentence(sentence);
        result.setAccuracy(accuracy);
        result.setRecognizedText(recognizedText);
        result.setUserAudioUrl(userAudioUrl);

        return speakingResultDAO.save(result);
    }

    @Override
    public List<SpeakingResult> getResultsByUserId(Long userId) {
        return speakingResultDAO.findByUserId(userId);
    }
}
