package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileUserProfileResponse;
import com.english.learning.entity.User;
import com.english.learning.service.user.UserService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller: Mobile User Profile API.
 * Provides JSON endpoints for Android profile screen.
 */
@RestController
@RequestMapping("/api/mobile/profile")
@RequiredArgsConstructor
public class MobileProfileController {

    private final UserService userService;

    /**
     * GET /api/mobile/profile/{userId}
     * Returns user profile information.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<MobileUserProfileResponse> getProfile(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        String formattedTime = TimeFormatUtil.formatActiveTime(
                user.getTotalActiveTime() != null ? user.getTotalActiveTime() : 0);

        MobileUserProfileResponse response = MobileUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatarUrl(user.getAvatarUrl())
                .totalActiveTime(user.getTotalActiveTime())
                .formattedActiveTime(formattedTime)
                .activeTime7d(user.getActiveTime7d())
                .activeTime30d(user.getActiveTime30d())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/mobile/profile/{userId}/username
     * Body: { "newUsername": "..." }
     */
    @PutMapping("/{userId}/username")
    public ResponseEntity<?> updateUsername(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String newUsername = payload.get("newUsername");
        if (newUsername == null || newUsername.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "newUsername required"));
        }

        try {
            userService.updateUsername(userId, newUsername);
            return ResponseEntity.ok(Map.of("success", true, "username", newUsername.trim()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
