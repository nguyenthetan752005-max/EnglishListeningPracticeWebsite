package com.english.learning.controller.api;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.service.SpeakingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/speaking")
public class SpeakingApiController {

    private final SpeakingService speakingService;

    public SpeakingApiController(SpeakingService speakingService) {
        this.speakingService = speakingService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<SpeakingResultDTO> evaluateSpeaking(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("referenceText") String referenceText) {
        try {
            SpeakingResultDTO result = speakingService.evaluateSpeaking(audio, referenceText);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}
