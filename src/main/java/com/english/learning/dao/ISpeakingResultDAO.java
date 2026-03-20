package com.english.learning.dao;

import com.english.learning.model.SpeakingResult;

import java.util.List;
import java.util.Optional;

public interface ISpeakingResultDAO {
    Optional<SpeakingResult> findByUserIdAndSentenceId(Long userId, Long sentenceId);
    List<SpeakingResult> findByUserId(Long userId);
    SpeakingResult save(SpeakingResult speakingResult);
}
