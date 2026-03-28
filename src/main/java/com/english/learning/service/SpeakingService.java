package com.english.learning.service;

import com.english.learning.dto.SpeakingResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface SpeakingService {
    SpeakingResultDTO evaluateSpeaking(MultipartFile audio, String referenceText);
}
