package com.english.learning.controller;

import com.english.learning.entity.User;
import com.english.learning.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

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
            model.addAttribute("user", userOpt.get());
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
}
