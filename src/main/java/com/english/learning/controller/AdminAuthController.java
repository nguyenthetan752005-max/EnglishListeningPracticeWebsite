package com.english.learning.controller;

import com.english.learning.dto.AdminDashboardDTO;
import com.english.learning.entity.User;
import com.english.learning.service.AdminDashboardService;
import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserService userService;
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/login")
    public String adminLogin(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin != null) {
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String doAdminLogin(@RequestParam(value = "username", required = false) String username,
                               @RequestParam(value = "password", required = false) String password,
                               Model model, HttpSession session) {
        Optional<User> adminOpt = userService.authenticateAdmin(username, password);
        if (adminOpt.isPresent()) {
            session.setAttribute("loggedInAdmin", adminOpt.get());
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid credentials or insufficient permissions!");
            return "admin/login";
        }
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.removeAttribute("loggedInAdmin");
        return "redirect:/admin/login";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        // Refresh admin data from DB
        Optional<User> freshAdmin = userService.findById(admin.getId());
        if (freshAdmin.isPresent()) {
            admin = freshAdmin.get();
            session.setAttribute("loggedInAdmin", admin);
        }

        // Delegate ALL data aggregation to Service (Thin Controller / SRP)
        AdminDashboardDTO dashboard = adminDashboardService.getDashboardData();

        model.addAttribute("totalUsers", dashboard.getTotalUsers());
        model.addAttribute("totalAdmins", dashboard.getTotalAdmins());
        model.addAttribute("totalLessons", dashboard.getTotalLessons());
        model.addAttribute("totalTime", dashboard.getFormattedTotalTime());
        model.addAttribute("recentUsers", dashboard.getRecentUsers());
        model.addAttribute("regularUsers", dashboard.getRegularUsers());
        model.addAttribute("adminUsers", dashboard.getAdminUsers());
        model.addAttribute("allLessons", dashboard.getAllLessons());
        model.addAttribute("deletedUsers", dashboard.getDeletedUsers());
        model.addAttribute("deletedSentences", dashboard.getDeletedSentences());
        model.addAttribute("admin", admin);

        return "admin/dashboard";
    }
}
