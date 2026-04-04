package com.english.learning.service.impl.auth;

import com.english.learning.entity.PasswordResetToken;
import com.english.learning.entity.User;
import com.english.learning.repository.PasswordResetTokenRepository;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.auth.PasswordResetService;
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
    private final AuthService authService;
    private final JavaMailSender mailSender;

    @Value("${app.url}")
    private String appUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public String createTokenForUser(User user) {
        // XÃ³a token cÅ© náº¿u cÃ³
        tokenRepository.deleteByUser(user);

        // Táº¡o token má»›i
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
        sendEmail(email, token, "forgot-password");
    }

    @Override
    public void sendProfilePasswordChangeEmail(String email, String token) {
        sendEmail(email, token, "user-profile");
    }

    @Override
    public void sendAdminPasswordChangeEmail(String email, String token) {
        sendEmail(email, token, "admin-profile");
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token khÃ´ng há»£p lá»‡!");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token Ä‘Ã£ háº¿t háº¡n! Vui lÃ²ng yÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u má»›i.");
        }

        User user = resetToken.getUser();
        authService.updatePassword(user, newPassword);

        // XÃ³a token sau khi Ä‘Ã£ sá»­ dá»¥ng
        tokenRepository.delete(resetToken);
    }

    private void sendEmail(String email, String token, String source) {
        boolean fromProfile = !"forgot-password".equals(source);
        boolean fromAdminProfile = "admin-profile".equals(source);
        String resetUrl = appUrl + "/reset-password?token=" + token + "&source=" + source;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject(fromProfile
                ? (fromAdminProfile
                    ? "English Learning - XÃ¡c nháº­n Ä‘á»•i máº­t kháº©u admin"
                    : "English Learning - XÃ¡c nháº­n Ä‘á»•i máº­t kháº©u")
                : "English Learning - Äáº·t láº¡i máº­t kháº©u");
        message.setText("Xin chÃ o,\n\n"
                + (fromAdminProfile
                ? "Báº¡n Ä‘Ã£ yÃªu cáº§u Ä‘á»•i máº­t kháº©u cho tÃ i khoáº£n admin Ä‘ang Ä‘Äƒng nháº­p. Vui lÃ²ng nháº¥n vÃ o link bÃªn dÆ°á»›i Ä‘á»ƒ xÃ¡c nháº­n vÃ  Ä‘áº·t máº­t kháº©u má»›i:\n\n"
                : (fromProfile
                ? "Báº¡n Ä‘Ã£ yÃªu cáº§u Ä‘á»•i máº­t kháº©u tá»« trang há»“ sÆ¡. Vui lÃ²ng nháº¥n vÃ o link bÃªn dÆ°á»›i Ä‘á»ƒ xÃ¡c nháº­n vÃ  Ä‘áº·t máº­t kháº©u má»›i:\n\n"
                : "Báº¡n Ä‘Ã£ yÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u. Vui lÃ²ng nháº¥n vÃ o link bÃªn dÆ°á»›i Ä‘á»ƒ Ä‘áº·t máº­t kháº©u má»›i:\n\n"))
                + resetUrl + "\n\n"
                + "Link nÃ y sáº½ háº¿t háº¡n sau " + TOKEN_EXPIRY_MINUTES + " phÃºt.\n\n"
                + (fromProfile
                ? "Sau khi Ä‘á»•i máº­t kháº©u thÃ nh cÃ´ng, phiÃªn Ä‘Äƒng nháº­p hiá»‡n táº¡i sáº½ bá»‹ Ä‘Äƒng xuáº¥t.\n\n"
                : "")
                + "Náº¿u báº¡n khÃ´ng yÃªu cáº§u thao tÃ¡c nÃ y, vui lÃ²ng bá» qua email nÃ y.\n\n"
                + "TrÃ¢n trá»ng,\nEnglish Learning Team");

        mailSender.send(message);
    }
}

