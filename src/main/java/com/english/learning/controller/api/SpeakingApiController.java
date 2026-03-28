package com.english.learning.controller.api;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.WitAIAudioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;

@RestController
@RequestMapping("/api/speaking")
public class SpeakingApiController {

    private final WitAIAudioService witAIAudioService;
    private final SentenceRepository sentenceRepository;

    public SpeakingApiController(WitAIAudioService witAIAudioService, SentenceRepository sentenceRepository) {
        this.witAIAudioService = witAIAudioService;
        this.sentenceRepository = sentenceRepository;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<SpeakingResultDTO> evaluateSpeaking(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("sentenceId") Long sentenceId) {
        try {
            // 1. Lấy reference text từ DB dựa trên sentenceId
            Sentence sentence = sentenceRepository.findById(sentenceId)
                    .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));
            String referenceText = sentence.getContent();

            // 2. Gửi file ghi âm qua Whisper để nhận dạng văn bản
            String transcribedText = witAIAudioService.transcribeAudio(audio);

            // 3. Chấm điểm văn bản vừa nhận dạng với câu mẫu
            SpeakingResultDTO result = calculateResult(referenceText, transcribedText);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private SpeakingResultDTO calculateResult(String reference, String transcribed) {
        String refClean = reference.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().trim();
        String transClean = transcribed.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase().trim();

        String[] refWords = refClean.split("\\s+");
        String[] transWords = transClean.split("\\s+");

        int matchCount = 0;
        // Basic evaluation checking for presence of target words.
        for (String word : refWords) {
            if (Arrays.asList(transWords).contains(word)) {
                matchCount++;
            }
        }

        int score = 0;
        if (refWords.length > 0) {
            score = Math.min((matchCount * 100) / refWords.length, 100);
        }

        SpeakingResultDTO result = new SpeakingResultDTO();
        result.setReferenceText(reference);
        result.setTranscribedText(transcribed);
        result.setScore(score);

        // Highlight logic can be generated or sent as HTML text if needed,
        // Here we just use a generic feedback message based on score
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
