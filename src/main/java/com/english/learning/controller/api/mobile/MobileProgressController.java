package com.english.learning.controller.api.mobile;

import com.english.learning.dto.InProgressLessonDTO;
import com.english.learning.entity.UserProgress;
import com.english.learning.service.progress.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller: Mobile User Progress API.
 * Provides JSON endpoints for Android to track learning progress.
 */
@RestController
@RequestMapping("/api/mobile/progress")
@RequiredArgsConstructor
public class MobileProgressController {

    private final UserProgressService userProgressService;

    /**
     * GET /api/mobile/progress/in-progress?userId={userId}
     * Returns list of in-progress lessons for a user.
     */
    @GetMapping("/in-progress")
    public ResponseEntity<List<InProgressLessonDTO>> getInProgressLessons(@RequestParam Long userId) {
        List<InProgressLessonDTO> lessons = userProgressService.getInProgressLessons(userId);
        return ResponseEntity.ok(lessons);
    }

    /**
     * POST /api/mobile/progress/update
     * Body: { "userId": 1, "sentenceId": 100 }
     * Updates progress for a sentence (marks as IN_PROGRESS).
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        Long sentenceId = payload.get("sentenceId");
        if (userId == null || sentenceId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId and sentenceId required"));
        }
        UserProgress progress = userProgressService.updateProgress(userId, sentenceId);
        return ResponseEntity.ok(Map.of("success", true, "status", progress.getStatus().name()));
    }

    /**
     * POST /api/mobile/progress/complete
     * Body: { "userId": 1, "sentenceId": 100 }
     * Marks a sentence as COMPLETED.
     */
    @PostMapping("/complete")
    public ResponseEntity<?> completeSentence(@RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        Long sentenceId = payload.get("sentenceId");
        if (userId == null || sentenceId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId and sentenceId required"));
        }
        UserProgress progress = userProgressService.completeSentence(userId, sentenceId);
        return ResponseEntity.ok(Map.of("success", true, "status", progress.getStatus().name()));
    }

    /**
     * POST /api/mobile/progress/skip
     * Body: { "userId": 1, "sentenceId": 100 }
     * Marks a sentence as SKIPPED.
     */
    @PostMapping("/skip")
    public ResponseEntity<?> skipSentence(@RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        Long sentenceId = payload.get("sentenceId");
        if (userId == null || sentenceId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId and sentenceId required"));
        }
        UserProgress progress = userProgressService.skipSentence(userId, sentenceId);
        return ResponseEntity.ok(Map.of("success", true, "status", progress.getStatus().name()));
    }
}
