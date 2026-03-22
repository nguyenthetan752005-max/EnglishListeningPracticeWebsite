package com.english.learning.controller;

import com.english.learning.entity.User;
import com.english.learning.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}
