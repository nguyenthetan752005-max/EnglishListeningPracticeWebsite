// INTERFACE DỊCH VỤ CHẤM CHÍNH TẢ.
// - Định nghĩa hợp đồng (contract) cho DictationServiceImpl.
// - Tuân thủ nguyên lý D trong SOLID (Dependency Inversion Principle).

package com.english.learning.service;

import com.english.learning.dto.DictationResultDTO;

public interface DictationService {

    /**
     * Chấm đáp án chép chính tả theo từng từ.
     * @param sentenceId ID của câu trong Database
     * @param userInput  Nội dung người dùng gõ vào
     * @return DTO chứa kết quả (hint, matchedCount, isCorrect...)
     */
    DictationResultDTO checkAnswer(Long sentenceId, String userInput);

    /**
     * Lấy đáp án đầy đủ (dùng cho chức năng Skip).
     * @param sentenceId ID của câu trong Database
     * @return DTO chứa câu gốc đầy đủ
     */
    DictationResultDTO skipSentence(Long sentenceId);
}
