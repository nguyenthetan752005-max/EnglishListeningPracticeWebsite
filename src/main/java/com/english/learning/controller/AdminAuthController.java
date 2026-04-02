package com.english.learning.controller;

import com.english.learning.dto.AdminDashboardDTO;
import com.english.learning.dto.AppSettingForm;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.AdminDashboardService;
import com.english.learning.service.AppSettingService;
import com.english.learning.service.AuthService;
import com.english.learning.service.CategoryService;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.PasswordResetService;
import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final AdminDashboardService adminDashboardService;
    private final UserProgressRepository userProgressRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final PasswordResetService passwordResetService;
    private final CloudinaryService cloudinaryService;
    private final AppSettingService appSettingService;

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
        Optional<User> adminOpt = authService.authenticateAdmin(username, password);
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
    public String adminDashboard(@RequestParam(value = "tab", required = false) String initialTab,
                                 HttpSession session, Model model) {
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
        model.addAttribute("deletedCategories", dashboard.getDeletedCategories());
        model.addAttribute("deletedSections", dashboard.getDeletedSections());
        model.addAttribute("deletedLessons", dashboard.getDeletedLessons());
        model.addAttribute("deletedSentences", dashboard.getDeletedSentences());
        model.addAttribute("deletedComments", dashboard.getDeletedComments());
        model.addAttribute("deletedSlideshows", dashboard.getDeletedSlideshows());
        model.addAttribute("recentComments", dashboard.getRecentComments());
        model.addAttribute("slideshows", dashboard.getSlideshows());
        model.addAttribute("admin", admin);
        model.addAttribute("initialTab", initialTab);
        if (!model.containsAttribute("settingForm")) {
            model.addAttribute("settingForm", appSettingService.getSettingForm());
        }
        boolean forceAdminOverviewTab = Boolean.TRUE.equals(session.getAttribute("forceAdminOverviewTab"));
        model.addAttribute("forceAdminOverviewTab", forceAdminOverviewTab);
        if (forceAdminOverviewTab) {
            session.removeAttribute("forceAdminOverviewTab");
        }

        return "admin/dashboard";
    }

    @PostMapping("/settings")
    public String updateSettings(@Valid AppSettingForm settingForm,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.settingForm", bindingResult);
            redirectAttributes.addFlashAttribute("settingForm", settingForm);
            redirectAttributes.addFlashAttribute("settingsError",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/dashboard?tab=settings";
        }

        appSettingService.updateSettings(settingForm);
        redirectAttributes.addFlashAttribute("settingsSuccess", "Settings updated successfully.");
        return "redirect:/admin/dashboard?tab=settings";
    }

    @GetMapping("/users/{id}/profile")
    public String adminUserProfile(@PathVariable Long id,
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
        model.addAttribute("roles", java.util.List.of(Role.USER));
        model.addAttribute("isSelfAdmin", false);
        model.addAttribute("canEditUser", true);
        model.addAttribute("canEditSelfAdmin", false);
        model.addAttribute("returnTab", "users");
        return "admin/user-profile";
    }

    @GetMapping("/profile")
    public String adminSelfProfile(HttpSession session, Model model) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        Optional<User> freshAdmin = userService.findById(admin.getId());
        if (freshAdmin.isEmpty()) {
            return "redirect:/admin/login";
        }

        admin = freshAdmin.get();
        session.setAttribute("loggedInAdmin", admin);
        model.addAttribute("admin", admin);
        model.addAttribute("userDetail", admin);
        model.addAttribute("recentProgress", java.util.List.of());
        model.addAttribute("progressCompleted", 0);
        model.addAttribute("progressInProgress", 0);
        model.addAttribute("progressSkipped", 0);
        model.addAttribute("topScore", 0);
        model.addAttribute("avgScore", "0.0");
        model.addAttribute("roles", Role.values());
        model.addAttribute("isSelfAdmin", true);
        model.addAttribute("canEditUser", false);
        model.addAttribute("canEditSelfAdmin", true);
        model.addAttribute("returnTab", "admins");
        return "admin/user-profile";
    }

    @PostMapping("/profile/update")
    public String updateSelfAdminProfile(@RequestParam("username") String username,
                                         @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                         HttpSession session,
                                         org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        try {
            User freshAdmin = userService.findById(admin.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản admin."));
            userService.adminUpdateBasicInfo(freshAdmin.getId(), username, avatarUrl, Boolean.TRUE.equals(freshAdmin.getIsActive()), freshAdmin.getRole());
            User updatedAdmin = userService.findById(freshAdmin.getId()).orElse(freshAdmin);
            session.setAttribute("loggedInAdmin", updatedAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin admin.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/profile/update-avatar")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> updateAdminAvatar(@RequestParam("avatar") MultipartFile file,
                                                                           HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            response.put("success", false);
            response.put("message", "Chưa đăng nhập admin");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Optional<User> freshAdminOpt = userService.findById(admin.getId());
            User freshAdmin = freshAdminOpt.orElseThrow(() -> new RuntimeException("Không tìm thấy admin"));
            if (freshAdmin.getAvatarPublicId() != null && !freshAdmin.getAvatarPublicId().isBlank()) {
                cloudinaryService.deleteFile(freshAdmin.getAvatarPublicId());
            }
            java.util.Map<String, String> uploadResult = cloudinaryService.uploadFile(
                    file,
                    "image",
                    "avatars/admins",
                    "admin_avatar_" + admin.getId(),
                    true
            );
            String avatarUrl = uploadResult.get("url");
            String avatarPublicId = uploadResult.get("publicId");
            userService.updateAvatar(admin.getId(), avatarUrl, avatarPublicId);

            admin.setAvatarUrl(avatarUrl);
            admin.setAvatarPublicId(avatarPublicId);
            session.setAttribute("loggedInAdmin", admin);

            response.put("success", true);
            response.put("avatarUrl", avatarUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/users/{id}/profile/update")
    public String updateUserProfileByAdmin(@PathVariable Long id,
                                           @RequestParam("username") String username,
                                           @RequestParam(value = "avatarUrl", required = false) String avatarUrl,
                                           @RequestParam("isActive") boolean isActive,
                                           @RequestParam("role") Role role,
                                           HttpSession session,
                                           org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        try {
            userService.adminUpdateBasicInfo(id, username, avatarUrl, isActive, role);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật thông tin người dùng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users/" + id + "/profile";
    }

    @PostMapping("/users/{id}/profile/password")
    public String updateUserPasswordByAdmin(@PathVariable Long id,
                                            @RequestParam("newPassword") String newPassword,
                                            @RequestParam("confirmPassword") String confirmPassword,
                                            HttpSession session,
                                            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu xác nhận không khớp.");
            return "redirect:/admin/users/" + id + "/profile";
        }

        try {
            userService.adminUpdatePassword(id, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mật khẩu người dùng.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/users/" + id + "/profile";
    }

    @PostMapping("/profile/request-password-change")
    public String requestAdminPasswordChange(HttpSession session,
                                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        User admin = (User) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            return "redirect:/admin/login";
        }
        Optional<User> freshAdmin = userService.findById(admin.getId());
        if (freshAdmin.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy tài khoản admin hiện tại.");
            return "redirect:/admin/profile";
        }
        try {
            User targetAdmin = freshAdmin.get();
            String token = passwordResetService.createTokenForUser(targetAdmin);
            passwordResetService.sendAdminPasswordChangeEmail(targetAdmin.getEmail(), token);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đã gửi email xác nhận đổi mật khẩu admin. Sau khi đổi thành công, bạn sẽ bị đăng xuất.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Không thể gửi email đổi mật khẩu admin. Vui lòng thử lại sau.");
        }
        return "redirect:/admin/profile";
    }
}
