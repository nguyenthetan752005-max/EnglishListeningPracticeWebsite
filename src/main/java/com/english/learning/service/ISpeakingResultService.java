package com.english.learning.service;

import com.english.learning.model.SpeakingResult;

import java.util.List;

public interface ISpeakingResultService {
    SpeakingResult saveSpeakingResult(Long userId, Long sentenceId, Double accuracy, String recognizedText, String userAudioUrl);
    List<SpeakingResult> getResultsByUserId(Long userId);
}
