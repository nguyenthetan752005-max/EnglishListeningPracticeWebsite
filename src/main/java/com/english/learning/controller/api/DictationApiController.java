// REST API TIẾP NHẬN YÊU CẦU CHẤM CHÍNH TẢ TỪ TRÌNH DUYỆT.
// - Nhận sentenceId + userInput qua HTTP POST.
// - Gọi DictationService để xử lý logic.
// - Trả kết quả JSON (DictationResultDTO) về cho JavaScript.

package com.english.learning.controller.api;

import com.english.learning.dto.DictationResultDTO;
import com.english.learning.service.DictationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/dictation")
public class DictationApiController {

    @Autowired
    private DictationService dictationService;

    /**
     * API Chấm đáp án chép chính tả.
     * POST /api/dictation/check
     * Body: { "sentenceId": 123, "userInput": "it snowed all" }
     * Response: { "correct": false, "matchedCount": 3, "hint": "it snowed all *** ***", ... }
     */
    @PostMapping("/check")
    public ResponseEntity<DictationResultDTO> checkAnswer(@RequestBody Map<String, Object> request) {
        Long sentenceId = Long.valueOf(request.get("sentenceId").toString());
        String userInput = (String) request.get("userInput");

        // Kiểm tra input rỗng
        if (userInput == null || userInput.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        DictationResultDTO result = dictationService.checkAnswer(sentenceId, userInput);
        return ResponseEntity.ok(result);
    }

    /**
     * API Bỏ qua câu (Skip) - trả về đáp án đầy đủ.
     * POST /api/dictation/skip
     * Body: { "sentenceId": 123 }
     * Response: { "correct": false, "hint": "It snowed all day today.", "correctSentence": "..." }
     */
    @PostMapping("/skip")
    public ResponseEntity<DictationResultDTO> skipSentence(@RequestBody Map<String, Object> request) {
        Long sentenceId = Long.valueOf(request.get("sentenceId").toString());

        DictationResultDTO result = dictationService.skipSentence(sentenceId);
        return ResponseEntity.ok(result);
    }
}
