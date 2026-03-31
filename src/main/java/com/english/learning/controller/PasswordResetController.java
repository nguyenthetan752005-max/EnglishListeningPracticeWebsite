package com.english.learning.controller;

import com.english.learning.entity.User;
import com.english.learning.service.PasswordResetService;
import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    // === FORGOT PASSWORD (Nhập email) ===

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "auth/recover";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Không tìm thấy tài khoản với email này!");
            return "auth/recover";
        }

        User user = userOpt.get();

        try {
            String token = passwordResetService.createTokenForUser(user);
            passwordResetService.sendResetEmail(user.getEmail(), token);
            model.addAttribute("success", "Đã gửi link đặt lại mật khẩu tới email của bạn. Vui lòng kiểm tra hộp thư!");
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi gửi email. Vui lòng thử lại sau!");
        }

        return "auth/recover";
    }

    // === RESET PASSWORD (Đặt mật khẩu mới) ===

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/resetpassword";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            model.addAttribute("token", token);
            return "auth/resetpassword";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 6 ký tự!");
            model.addAttribute("token", token);
            return "auth/resetpassword";
        }

        try {
            passwordResetService.resetPassword(token, password);
            model.addAttribute("success", "Đặt lại mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.");
            return "auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("token", token);
            return "auth/resetpassword";
        }
    }
}
