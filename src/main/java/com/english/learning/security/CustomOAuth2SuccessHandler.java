package com.english.learning.security;

import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.AppSettingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AppSettingService appSettingService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email = token.getPrincipal().getAttribute("email");
        String picture = token.getPrincipal().getAttribute("picture");

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            // Đã tồn tại Email, đánh dấu liên kết Google
            if (user.getProvider() == null || !user.getProvider().equals("GOOGLE")) {
                user.setProvider("GOOGLE");
                if (user.getAvatarUrl() == null) {
                    user.setAvatarUrl(picture);
                }
            }
            // Mark as online on OAuth login
            user.setIsActive(true);
            user = userRepository.save(user);
        } else {
            if (!appSettingService.isUserRegistrationAllowed()) {
                getRedirectStrategy().sendRedirect(request, response, "/login?error=registration-disabled");
                return;
            }
            // Chưa tồn tại Email -> Tạo tài khoản mới
            user = new User();
            user.setEmail(email);
            // Lấy phần đầu của email làm username, thêm random _uuid để tránh trùng lặp
            String baseUsername = email.split("@")[0];
            if (userRepository.findByUsername(baseUsername).isPresent()) {
                baseUsername = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
            }
            user.setUsername(baseUsername);

            // Khởi tạo mật khẩu an toàn ngẫu nhiên
            user.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt()));
            user.setProvider("GOOGLE");
            user.setAvatarUrl(picture);
            user.setIsActive(true);
            user = userRepository.save(user);
        }

        // Lưu user vào session giống như cách code ở AuthController
        request.getSession().setAttribute("loggedInUser", user);

        // Chuyển hướng người dùng về trang profile
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
