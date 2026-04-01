package com.english.learning.controller;

import com.english.learning.entity.User;
import com.english.learning.service.PasswordResetService;
import com.english.learning.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import com.english.learning.service.CloudinaryService;
import com.english.learning.util.TimeFormatUtil;

import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        // Lấy Session user sau khi đăng nhập
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            // Nếu chưa đăng nhập thì đẩy về màn login
            return "redirect:/login";
        }

        // Lấy dữ liệu mới nhất từ DB đề phòng update sửa lỗi
        Optional<User> userOpt = userService.findById(loggedInUser.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            model.addAttribute("user", user);
            
            // Format total active time (MVC - Controller prepares data for View)
            String formattedTime = TimeFormatUtil.formatActiveTime(user.getTotalActiveTime() != null ? user.getTotalActiveTime() : 0);
            model.addAttribute("formattedActiveTime", formattedTime);
            
            return "user/profile";
        }

        return "redirect:/login";
    }

    @PostMapping("/profile/update-name")
    public String updateUsername(@RequestParam("newUsername") String newUsername,
                                 HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            userService.updateUsername(loggedInUser.getId(), newUsername);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tên thành công!");
            // Cập nhật lại session
            loggedInUser.setUsername(newUsername);
            session.setAttribute("loggedInUser", loggedInUser);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/update-avatar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAvatar(@RequestParam("avatar") MultipartFile file,
                                                            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "Chưa đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> freshUserOpt = userService.findById(loggedInUser.getId());
            User freshUser = freshUserOpt.orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
            if (freshUser.getAvatarPublicId() != null && !freshUser.getAvatarPublicId().isBlank()) {
                cloudinaryService.deleteFile(freshUser.getAvatarPublicId());
            }
            Map<String, String> uploadResult = cloudinaryService.uploadFile(
                    file,
                    "image",
                    "avatars/users",
                    "user_avatar_" + loggedInUser.getId(),
                    true
            );
            String avatarUrl = uploadResult.get("url");
            String avatarPublicId = uploadResult.get("publicId");
            userService.updateAvatar(loggedInUser.getId(), avatarUrl, avatarPublicId);
            
            loggedInUser.setAvatarUrl(avatarUrl);
            loggedInUser.setAvatarPublicId(avatarPublicId);
            session.setAttribute("loggedInUser", loggedInUser);
            
            response.put("success", true);
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/profile/request-password-change")
    public String requestPasswordChange(HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }

        Optional<User> userOpt = userService.findById(loggedInUser.getId());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản hiện tại.");
            return "redirect:/profile";
        }

        try {
            User user = userOpt.get();
            String token = passwordResetService.createTokenForUser(user);
            passwordResetService.sendProfilePasswordChangeEmail(user.getEmail(), token);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gửi email xác nhận đổi mật khẩu. Sau khi đổi thành công, bạn sẽ bị đăng xuất.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể gửi email đổi mật khẩu. Vui lòng thử lại sau.");
        }

        return "redirect:/profile";
    }
}
