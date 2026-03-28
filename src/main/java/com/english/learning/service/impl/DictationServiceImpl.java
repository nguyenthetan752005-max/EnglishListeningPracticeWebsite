// TRIỂN KHAI THUẬT TOÁN CHẤM CHÍNH TẢ THEO TỪNG TỪ.
// - So sánh từng từ người dùng gõ với đáp án trong Database.
// - BỎ QUA dấu câu (.,?!;:) khi so sánh nhưng GIỮ NGUYÊN khi hiển thị.
// - Tạo mảng gợi ý: từ đúng giữ nguyên, từ chưa đúng thành "***".
// - Đánh dấu chỉ số từ MỚI được gợi ý (newHintIndex) để Frontend tô xanh.

package com.english.learning.service.impl;

import com.english.learning.dto.DictationResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.exception.SentenceNotFoundException;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.DictationService;
import com.english.learning.service.TextComparisonService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictationServiceImpl implements DictationService {

    private final SentenceRepository sentenceRepository;
    private final TextComparisonService textComparisonService;

    public DictationServiceImpl(SentenceRepository sentenceRepository, TextComparisonService textComparisonService) {
        this.sentenceRepository = sentenceRepository;
        this.textComparisonService = textComparisonService;
    }

    /**
     * Chấm đáp án theo từng từ. (Sử dụng TextComparisonService chung)
     */
    @Override
    public DictationResultDTO checkAnswer(Long sentenceId, String userInput) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        return textComparisonService.compareSequential(sentence.getContent(), userInput);
    }

    /**
     * Chức năng Skip: Trả về toàn bộ đáp án.
     */
    @Override
    public DictationResultDTO skipSentence(Long sentenceId) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        String correctContent = sentence.getContent();
        String[] correctWords = correctContent.trim().split("\\s+");

        List<String> hintWords = new ArrayList<>();
        for (String w : correctWords) {
            hintWords.add(w);
        }

        return new DictationResultDTO(
                false,
                0,
                correctWords.length,
                hintWords,
                -1,
                correctContent
        );
    }
}
