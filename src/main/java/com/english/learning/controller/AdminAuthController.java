package com.english.learning.controller;

import com.english.learning.dto.AdminDashboardDTO;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.AdminDashboardService;
import com.english.learning.service.CategoryService;
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
import java.util.Set;
import java.util.TreeSet;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final AdminDashboardService adminDashboardService;
    private final UserProgressRepository userProgressRepository;
    private final SpeakingResultRepository speakingResultRepository;

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
            User admin = adminOpt.get();
            userService.updateActiveStatus(admin.getId(), true);
            session.setAttribute("loggedInAdmin", admin);
            session.setAttribute("forceAdminOverviewTab", true);
            return "redirect:/admin/dashboard";
        } else {
            model.addAttribute("error", "Invalid credentials or insufficient permissions!");
            return "admin/login";
        }
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin != null) {
            userService.updateActiveStatus(admin.getId(), false);
        }
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
        model.addAttribute("userTopScoreMap", dashboard.getUserTopScoreMap());
        model.addAttribute("adminUsers", dashboard.getAdminUsers());

        // Content hierarchy
        model.addAttribute("categories", dashboard.getCategories());
        model.addAttribute("categoryLevelsMap", dashboard.getCategories().stream()
                .collect(java.util.stream.Collectors.toMap(
                        com.english.learning.entity.Category::getId,
                        category -> String.join(",", categoryService.getExpandedLevels(category.getLevelRange()))
                )));
        model.addAttribute("categoryLevelOptions", dashboard.getCategories().stream()
                .flatMap(category -> categoryService.getExpandedLevels(category.getLevelRange()).stream())
                .map(String::trim)
                .filter(level -> !level.isEmpty())
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new)));
        model.addAttribute("lessonLevelOptions", dashboard.getAllLessons() == null ? Set.of() :
                dashboard.getAllLessons().stream()
                        .map(com.english.learning.entity.Lesson::getLevel)
                        .filter(java.util.Objects::nonNull)
                        .map(String::trim)
                        .filter(level -> !level.isEmpty())
                        .collect(java.util.stream.Collectors.toCollection(TreeSet::new)));

        // Other management data
        model.addAttribute("deletedUsers", dashboard.getDeletedUsers());
        model.addAttribute("deletedSentences", dashboard.getDeletedSentences());
        model.addAttribute("recentComments", dashboard.getRecentComments());
        model.addAttribute("slideshows", dashboard.getSlideshows());
        model.addAttribute("admin", admin);
        boolean forceAdminOverviewTab = Boolean.TRUE.equals(session.getAttribute("forceAdminOverviewTab"));
        model.addAttribute("forceAdminOverviewTab", forceAdminOverviewTab);
        if (forceAdminOverviewTab) {
            session.removeAttribute("forceAdminOverviewTab");
        }

        return "admin/dashboard";
    }

    @GetMapping("/users/{id}/profile")
    public String adminUserProfile(@org.springframework.web.bind.annotation.PathVariable Long id,
                                   HttpSession session,
                                   Model model) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        User user = userService.findById(id).orElse(null);
        if (user == null) {
            return "redirect:/admin/dashboard";
        }

        java.util.List<com.english.learning.entity.UserProgress> recentProgress =
                userProgressRepository.findTop100ByUser_IdOrderByLastAccessedDesc(id);
        long completed = recentProgress.stream().filter(p -> p.getStatus() == UserProgressStatus.COMPLETED).count();
        long inProgress = recentProgress.stream().filter(p -> p.getStatus() == UserProgressStatus.IN_PROGRESS).count();
        long skipped = recentProgress.stream().filter(p -> p.getStatus() == UserProgressStatus.SKIPPED).count();

        java.util.List<SpeakingResult> speakingResults = speakingResultRepository.findByUser_Id(id);
        int topScore = speakingResults.stream()
                .map(SpeakingResult::getScore)
                .filter(java.util.Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(0);
        double averageScore = speakingResults.stream()
                .map(SpeakingResult::getScore)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        model.addAttribute("admin", admin);
        model.addAttribute("userDetail", user);
        model.addAttribute("recentProgress", recentProgress);
        model.addAttribute("progressCompleted", completed);
        model.addAttribute("progressInProgress", inProgress);
        model.addAttribute("progressSkipped", skipped);
        model.addAttribute("topScore", topScore);
        model.addAttribute("avgScore", String.format(java.util.Locale.US, "%.1f", averageScore));
        return "admin/user-profile";
    }
}
