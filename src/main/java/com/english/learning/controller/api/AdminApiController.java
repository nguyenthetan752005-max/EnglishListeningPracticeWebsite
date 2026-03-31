package com.english.learning.controller.api;

import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.SectionService;
import com.english.learning.service.SentenceService;
import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final UserService userService;
    private final SectionService sectionService;
    private final SentenceService sentenceService;
    private final UserRepository userRepository;

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            userService.softDeleteUser(id);
            response.put("success", true);
            response.put("message", "Đã xóa mềm người dùng thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteSection(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            sectionService.deleteSection(id);
            response.put("success", true);
            response.put("message", "Đã xóa mềm Section thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/trash/sentences/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSentence(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            sentenceService.hardDeleteSentence(id);
            response.put("success", true);
            response.put("message", "Đã xóa vĩnh viễn Sentence");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/users/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreUser(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepository.findAnyUserById(id)
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
            user.setIsDeleted(false);
            user.setIsActive(true);
            userRepository.save(user);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
