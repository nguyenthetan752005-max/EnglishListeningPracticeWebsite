package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileLeaderboardResponse;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller: Mobile Leaderboard API.
 * Provides standalone leaderboard endpoint for Android.
 */
@RestController
@RequestMapping("/api/mobile/leaderboard")
@RequiredArgsConstructor
public class MobileLeaderboardController {

    private final MobileResponseMapper mapper;

    /**
     * GET /api/mobile/leaderboard
     * Returns weekly and monthly leaderboard entries.
     */
    @GetMapping
    public ResponseEntity<MobileLeaderboardResponse> getLeaderboard() {
        MobileLeaderboardResponse leaderboard = mapper.buildLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}
