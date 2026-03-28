package com.english.learning.service;

import com.english.learning.dto.DictationResultDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextComparisonService {

    /**
     * Chuẩn hóa từ: loại bỏ dấu câu, in thường.
     * Dùng cho việc so sánh nội dung mà bỏ qua định dạng lỗi nhỏ.
     */
    public String normalizeWord(String word) {
        if (word == null) return "";
        return word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
    }

    /**
     * Thuật toán chấm Dictation (Listening):
     * - So sánh tuần tự từ trái qua phải.
     * - Dừng lại ở từ sai đầu tiên.
     * - Tạo mảng gợi ý (hint) cho người dùng thấy được lỗi sai.
     */
    public DictationResultDTO compareSequential(String correctContent, String userInput) {
        String[] correctWords = correctContent.trim().split("\\s+");
        String[] userWords = userInput == null ? new String[0] : userInput.trim().split("\\s+");

        int matchedCount = 0;
        for (int i = 0; i < Math.min(correctWords.length, userWords.length); i++) {
            if (normalizeWord(correctWords[i]).equals(normalizeWord(userWords[i]))) {
                matchedCount++;
            } else {
                break; // Stop at first incorrect word (Dictation strict order rule)
            }
        }

        boolean isCorrect = (matchedCount == correctWords.length);

        List<String> hintWords = new ArrayList<>();
        int newHintIndex = -1;

        if (isCorrect) {
            for (String w : correctWords) {
                hintWords.add(w);
            }
        } else {
            int revealCount = Math.min(matchedCount + 1, correctWords.length);
            newHintIndex = matchedCount; 

            for (int i = 0; i < correctWords.length; i++) {
                if (i < revealCount) {
                    hintWords.add(correctWords[i]); 
                } else {
                    hintWords.add("***");
                }
            }
        }

        return new DictationResultDTO(
                isCorrect,
                matchedCount,
                correctWords.length,
                hintWords,
                newHintIndex,
                isCorrect ? correctContent : null
        );
    }
}
