package com.english.learning.service.impl.learning.dictation.strategy;

import com.english.learning.dto.DictationResultDTO;
import com.english.learning.util.TextNormalizerUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Strategy: Chấm điểm nới lỏng (Thuật toán gốc).
 * Bỏ qua dấu câu và không phân biệt chữ hoa/thường khi so sánh.
 */
@Component
public class RelaxedDictationCheckStrategy implements DictationCheckStrategy {

    @Override
    public String getStrategyName() {
        return "RELAXED";
    }

    @Override
    public DictationResultDTO check(String correctContent, String userInput) {
        String[] correctWords = correctContent.trim().split("\\s+");
        String[] userWords = userInput.trim().split("\\s+");

        int matchedCount = 0;
        for (int i = 0; i < Math.min(correctWords.length, userWords.length); i++) {
            if (TextNormalizerUtil.removePunctuationAndLowercase(correctWords[i])
                    .equals(TextNormalizerUtil.removePunctuationAndLowercase(userWords[i]))) {
                matchedCount++;
            } else {
                break;
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
