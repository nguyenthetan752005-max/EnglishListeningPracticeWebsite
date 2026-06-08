package com.english.learning.service.impl.learning.dictation;

import com.english.learning.dto.DictationResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.exception.SentenceNotFoundException;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.impl.learning.dictation.strategy.DictationCheckStrategy;
import com.english.learning.service.learning.dictation.DictationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DictationServiceImpl implements DictationService {

    private final SentenceRepository sentenceRepository;
    private final Map<String, DictationCheckStrategy> strategyMap;

    /**
     * Mẫu thiết kế Strategy Pattern kết hợp Factory (Map):
     * Spring sẽ tự động inject tất cả các class implements DictationCheckStrategy vào List<DictationCheckStrategy>.
     * Sau đó ta chuyển List thành Map để tra cứu thuật toán cực kỳ nhanh dựa vào tên Strategy (getStrategyName).
     */
    public DictationServiceImpl(SentenceRepository sentenceRepository, List<DictationCheckStrategy> strategies) {
        this.sentenceRepository = sentenceRepository;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(DictationCheckStrategy::getStrategyName, Function.identity()));
    }

    /**
     * Chấm điểm chính tả (Áp dụng Strategy Pattern)
     */
    @Override
    public DictationResultDTO checkAnswer(Long sentenceId, String userInput) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        // Tình huống thực tế: Có thể lấy chiến lược từ cài đặt người dùng hoặc tham số request.
        // Ở đây ta gán cứng "RELAXED" làm mặc định để không phá vỡ logic cũ của ứng dụng.
        String selectedStrategyName = "RELAXED"; 

        DictationCheckStrategy strategy = strategyMap.get(selectedStrategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Không tìm thấy chiến lược chấm điểm: " + selectedStrategyName);
        }

        // Ủy quyền (Delegate) xử lý thuật toán cho Strategy đã chọn
        return strategy.check(sentence.getContent(), userInput);
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
