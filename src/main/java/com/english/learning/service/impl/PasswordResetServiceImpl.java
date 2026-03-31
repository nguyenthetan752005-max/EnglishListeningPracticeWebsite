package com.english.learning.service.impl;

import com.english.learning.entity.PasswordResetToken;
import com.english.learning.entity.User;
import com.english.learning.repository.PasswordResetTokenRepository;
import com.english.learning.service.PasswordResetService;
import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserService userService;
    private final JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public String createTokenForUser(User user) {
        // Xóa token cũ nếu có
        tokenRepository.deleteByUser(user);

        // Tạo token mới
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));

        tokenRepository.save(token);
        return tokenValue;
    }

    @Override
    public void sendResetEmail(String email, String token) {
        String resetUrl = appUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("English Learning - Đặt lại mật khẩu");
        message.setText("Xin chào,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào link bên dưới để đặt mật khẩu mới:\n\n"
                + resetUrl + "\n\n"
                + "Link này sẽ hết hạn sau " + TOKEN_EXPIRY_MINUTES + " phút.\n\n"
                + "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\nEnglish Learning Team");

        mailSender.send(message);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token không hợp lệ!");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token đã hết hạn! Vui lòng yêu cầu đặt lại mật khẩu mới.");
        }

        User user = resetToken.getUser();
        userService.updatePassword(user, newPassword);

        // Xóa token sau khi đã sử dụng
        tokenRepository.delete(resetToken);
    }
}
