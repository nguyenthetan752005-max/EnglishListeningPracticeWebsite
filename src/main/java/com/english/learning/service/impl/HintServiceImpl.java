package com.english.learning.service.impl;

import com.english.learning.entity.Sentence;
import com.english.learning.service.HintService;
import com.english.learning.util.TextNormalizerUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HintServiceImpl implements HintService {

    private static final Set<String> EXCLUDED_WORDS = Set.of(
            "i", "me", "my", "mine", "myself",
            "we", "us", "our", "ours", "ourselves",
            "you", "your", "yours", "yourself", "yourselves",
            "he", "him", "his", "himself",
            "she", "her", "hers", "herself",
            "it", "its", "itself",
            "they", "them", "their", "theirs", "themselves"
    );

    @Override
    public List<String> extractProperNouns(String content) {
        List<String> properNouns = new ArrayList<>();
        if (content == null || content.isBlank()) return properNouns;

        String[] words = content.trim().split("\\s+");
        if (words.length <= 1) return properNouns;

        for (int i = 1; i < words.length; i++) {
            // Nếu từ trước kết thúc bằng . ? ! thì từ hiện tại là đầu câu mới → bỏ qua
            String prevWord = words[i - 1];
            if (prevWord.isEmpty()) continue;
            
            char lastChar = prevWord.charAt(prevWord.length() - 1);
            if (lastChar == '.' || lastChar == '?' || lastChar == '!') {
                continue;
            }

            // Kiểm tra ký tự đầu có viết hoa không
            String currentWord = words[i];
            if (currentWord.isEmpty()) continue;
            
            char firstChar = currentWord.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                // Loại bỏ dấu câu đi kèm (ví dụ: "Smith," → "Smith") bằng TextNormalizerUtil
                String cleanWord = TextNormalizerUtil.keepLettersAndHyphens(currentWord);
                if (!cleanWord.isEmpty() && !EXCLUDED_WORDS.contains(cleanWord.toLowerCase())) {
                    properNouns.add(cleanWord);
                }
            }
        }
        return properNouns;
    }

    @Override
    public Map<Long, List<String>> getHintsMap(List<Sentence> sentences) {
        return sentences.stream()
                .filter(s -> {
                    // Đảm bảo properNouns được trích xuất nếu chưa có
                    if (s.getProperNouns() == null || s.getProperNouns().isEmpty()) {
                        s.setProperNouns(extractProperNouns(s.getContent()));
                    }
                    return s.getProperNouns() != null && !s.getProperNouns().isEmpty();
                })
                .collect(Collectors.toMap(Sentence::getId, Sentence::getProperNouns));
    }
}
