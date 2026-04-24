package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileNotificationPreferenceRequest;
import com.english.learning.dto.mobile.MobileUserProfileResponse;
import com.english.learning.entity.User;
import com.english.learning.service.settings.AppSettingService;
import com.english.learning.service.user.UserService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
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

    private static final DateTimeFormatter REMINDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private final UserService userService;
    private final com.english.learning.service.auth.AuthService authService;
    private final com.english.learning.service.auth.TokenBlacklistService tokenBlacklistService;
    private final AppSettingService appSettingService;

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
                .notificationsEnabled(Boolean.TRUE.equals(user.getNotificationsEnabled()))
                .notificationTimezone(user.getNotificationTimezone())
                .dailyReminderEnabled(appSettingService.isDailyReminderEnabled())
                .dailyReminderTime(appSettingService.getDailyReminderTime().format(REMINDER_TIME_FORMATTER))
                .dailyReminderTimezone(appSettingService.getDailyReminderTimezone())
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

    /**
     * PUT /api/mobile/profile/{userId}/password
     * Body: { "currentPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/{userId}/password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) {
        String currentPassword = payload.get("currentPassword");
        String newPassword = payload.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "currentPassword and newPassword required"));
        }

        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu mới phải có ít nhất 6 ký tự."));
        }

        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        // Verify current password via AuthService
        Optional<User> authenticatedUser = authService.authenticateUser(user.getUsername(), currentPassword);
        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mật khẩu hiện tại không chính xác."));
        }

        try {
            authService.updatePassword(user, newPassword);
            // Logout from all devices
            tokenBlacklistService.revokeAllUserTokens(userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Có lỗi xảy ra: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/mobile/profile/{userId}/notifications
     * Body: { "notificationsEnabled": true, "timezone": "Asia/Bangkok" }
     */
    @PutMapping("/{userId}/notifications")
    public ResponseEntity<?> updateNotificationPreference(
            @PathVariable Long userId,
            @RequestBody MobileNotificationPreferenceRequest request) {
        if (request.getNotificationsEnabled() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "notificationsEnabled required"));
        }

        try {
            userService.updateNotificationPreference(
                    userId,
                    request.getNotificationsEnabled(),
                    request.getTimezone()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "notificationsEnabled", request.getNotificationsEnabled(),
                    "timezone", request.getTimezone()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
