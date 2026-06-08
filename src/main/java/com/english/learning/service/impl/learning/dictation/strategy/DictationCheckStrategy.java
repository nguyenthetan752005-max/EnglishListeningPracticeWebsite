package com.english.learning.service.impl.learning.dictation.strategy;

import com.english.learning.dto.DictationResultDTO;

/**
 * Strategy Pattern: Định nghĩa một Interface chung cho các thuật toán chấm điểm chính tả.
 */
public interface DictationCheckStrategy {
    
    /**
     * Tên của chiến lược để Factory/Context tra cứu (ví dụ: "STRICT", "RELAXED")
     */
    String getStrategyName();

    /**
     * Thuật toán chấm điểm chính tả.
     *
     * @param correctContent Câu đáp án gốc trong Database
     * @param userInput Nội dung người dùng nhập vào
     * @return DictationResultDTO chứa kết quả (số từ đúng, mảng gợi ý, v.v...)
     */
    DictationResultDTO check(String correctContent, String userInput);
}
