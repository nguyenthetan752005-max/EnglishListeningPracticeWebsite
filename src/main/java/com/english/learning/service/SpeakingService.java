package com.english.learning.service;

import com.english.learning.dto.SpeakingResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface SpeakingService {

    /**
     * Đánh giá speaking: transcribe → AI score → upload Cloudinary → lưu DB.
     * Nếu userId == null (chưa đăng nhập) thì vẫn chấm điểm nhưng không lưu.
     */
    SpeakingResultDTO evaluateSpeaking(MultipartFile audio, String referenceText, Long userId, Long sentenceId);

    /**
     * Lấy kết quả BEST + CURRENT đã lưu cho 1 câu (khi chuyển câu, load lại kết quả cũ).
     */
    SpeakingResultDTO getSavedResults(Long userId, Long sentenceId);
}
