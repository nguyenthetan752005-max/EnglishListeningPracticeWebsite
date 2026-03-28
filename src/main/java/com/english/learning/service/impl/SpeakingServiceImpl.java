package com.english.learning.service.impl;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.service.SpeakingService;
import com.english.learning.service.TextComparisonService;
import com.english.learning.service.WitAIAudioService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SpeakingServiceImpl implements SpeakingService {

    private final WitAIAudioService witAIAudioService;
    private final TextComparisonService textComparisonService;

    public SpeakingServiceImpl(WitAIAudioService witAIAudioService, TextComparisonService textComparisonService) {
        this.witAIAudioService = witAIAudioService;
        this.textComparisonService = textComparisonService;
    }

    @Override
    public SpeakingResultDTO evaluateSpeaking(MultipartFile audio, String referenceText) {
        // 1. Nhận dạng văn bản từ audio (sử dụng WitAI)
        String transcribedText;
        try {
            transcribedText = witAIAudioService.transcribeAudio(audio);
        } catch (Exception e) {
            throw new RuntimeException("Error transcribing audio", e);
        }

        // 2. Chấm điểm văn bản vừa nhận dạng với câu mẫu sử dụng thuật toán chung (LCS)
        int score = textComparisonService.calculateSpeakingScore(referenceText, transcribedText);

        SpeakingResultDTO result = new SpeakingResultDTO();
        result.setReferenceText(referenceText);
        result.setTranscribedText(transcribedText);
        result.setScore(score);

        // Feedback dựa trên điểm số
        if (score >= 90) {
            result.setFeedback("Excellent! You sound like a native.");
        } else if (score >= 70) {
            result.setFeedback("Good job! But there's room for improvement.");
        } else {
            result.setFeedback("Keep practicing! Make sure to pronounce every word clearly.");
        }

        return result;
    }
}
