package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileAuthResponse;
import com.english.learning.dto.mobile.MobileLoginRequest;
import com.english.learning.dto.mobile.MobileRegisterRequest;
import com.english.learning.dto.mobile.MobileGoogleAuthRequest;
import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.security.JwtTokenProvider;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.auth.GoogleAuthService;
import com.english.learning.service.settings.AppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * REST Controller: Mobile Authentication API.
 * Provides JSON login/register for Android app (stateless, no session).
 */
@RestController
@RequestMapping("/api/mobile/auth")
@RequiredArgsConstructor
public class MobileAuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final AppSettingService appSettingService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * POST /api/mobile/auth/login
     * Body: { "username": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<MobileAuthResponse> login(@RequestBody MobileLoginRequest request) {
        Optional<User> userOpt = authService.authenticateUser(request.getUsername(), request.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Kiểm tra trạng thái tài khoản (3 trạng thái: Hoạt động, Không hoạt động, Bị cấm)
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(false)
                        .code("ACCOUNT_BANNED")
                        .message("Tài khoản bị cấm" + (user.getSuspensionReason() != null ? ": " + user.getSuspensionReason() : ""))
                        .build());
            }
            // Đánh dấu user đang đăng nhập
            user.setIsActive(true);
            userRepository.save(user);

            // Tạo JWT token
            String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
            
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Login successful")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .avatarUrl(user.getAvatarUrl())
                    .token(token)
                    .build());
        }

        return ResponseEntity.ok(MobileAuthResponse.builder()
                .success(false)
                .message("Tài khoản hoặc mật khẩu không chính xác!")
                .build());
    }

    /**
     * POST /api/mobile/auth/register
     * Body: { "username": "...", "email": "...", "password": "..." }
     */
    @PostMapping("/register")
    public ResponseEntity<MobileAuthResponse> register(@RequestBody MobileRegisterRequest request) {
        if (!appSettingService.isUserRegistrationAllowed()) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Tính năng đăng ký tài khoản hiện đang bị khóa.")
                    .build());
        }

        try {
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            User savedUser = authService.register(user);

            // Tạo JWT token cho user mới
            String token = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());
            
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Registration successful")
                    .userId(savedUser.getId())
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole().name())
                    .avatarUrl(savedUser.getAvatarUrl())
                    .token(token)
                    .build());
        } catch (RuntimeException e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * POST /api/mobile/auth/google
     * Body: { "idToken": "..." }
     */
    @PostMapping("/google")
    public ResponseEntity<MobileAuthResponse> googleAuth(@RequestBody MobileGoogleAuthRequest request) {
        try {
            Optional<User> userOpt = googleAuthService.authenticateWithGoogle(request.getIdToken());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // Kiểm tra trạng thái tài khoản (3 trạng thái: Hoạt động, Không hoạt động, Bị cấm)
                if (Boolean.TRUE.equals(user.getIsDeleted())) {
                    return ResponseEntity.ok(MobileAuthResponse.builder()
                            .success(false)
                            .code("ACCOUNT_BANNED")
                            .message("Tài khoản bị cấm" + (user.getSuspensionReason() != null ? ": " + user.getSuspensionReason() : ""))
                            .build());
                }
                // Đánh dấu user đang đăng nhập
                user.setIsActive(true);
                userRepository.save(user);

                // Tạo JWT token
                String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
                
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(true)
                        .message("Đăng nhập Google thành công.")
                        .userId(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .avatarUrl(user.getAvatarUrl())
                        .token(token)
                        .build());
            } else {
                return ResponseEntity.ok(MobileAuthResponse.builder()
                        .success(false)
                        .message("Không thể xác thực với Google.")
                        .build());
            }
        } catch (RuntimeException e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * POST /api/mobile/auth/logout
     * Đăng xuất - đánh dấu user không còn đăng nhập
     */
    @PostMapping("/logout")
    public ResponseEntity<MobileAuthResponse> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenProvider.resolveToken(authHeader);
            if (token != null) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setIsActive(false);  // Đánh dấu đã đăng xuất
                    userRepository.save(user);
                }
                // TODO: Thêm token vào blacklist nếu cần
            }
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(true)
                    .message("Đăng xuất thành công.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.ok(MobileAuthResponse.builder()
                    .success(false)
                    .message("Lỗi đăng xuất: " + e.getMessage())
                    .build());
        }
    }
}
