package com.english.learning.controller.api;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.entity.Comment;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.Slideshow;
import com.english.learning.entity.User;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.SlideshowRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.CategoryService;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.LessonService;
import com.english.learning.service.SectionService;
import com.english.learning.service.SentenceService;
import com.english.learning.service.UserService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final SectionService sectionService;
    private final SentenceService sentenceService;
    private final LessonService lessonService;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final SlideshowRepository slideshowRepository;
    private final CloudinaryService cloudinaryService;

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteUser(@PathVariable Long id) {
        return handleAction(() -> userService.softDeleteUser(id), "Đã xóa mềm người dùng thành công");
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

    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(@Valid @RequestBody AdminCategoryRequest request) {
        return handleEntityAction(() -> categoryService.createCategory(request), "Đã tạo Category");
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(@PathVariable Long id,
                                                              @Valid @RequestBody AdminCategoryRequest request) {
        return handleEntityAction(() -> categoryService.updateCategory(id, request), "Đã cập nhật Category");
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        return handleAction(() -> categoryService.deleteCategory(id), "Đã xóa mềm Category");
    }

    @PostMapping("/sections")
    public ResponseEntity<Map<String, Object>> createSection(@Valid @RequestBody AdminSectionRequest request) {
        return handleEntityAction(() -> sectionService.createSection(request), "Đã tạo Section");
    }

    @PutMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> updateSection(@PathVariable Long id,
                                                             @Valid @RequestBody AdminSectionRequest request) {
        return handleEntityAction(() -> sectionService.updateSection(id, request), "Đã cập nhật Section");
    }

    @DeleteMapping("/sections/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteSection(@PathVariable Long id) {
        return handleAction(() -> sectionService.deleteSection(id), "Đã xóa mềm Section");
    }

    @PostMapping("/lessons")
    public ResponseEntity<Map<String, Object>> createLesson(@Valid @RequestBody AdminLessonRequest request) {
        return handleEntityAction(() -> lessonService.createLesson(request), "Đã tạo Lesson");
    }

    @PutMapping("/lessons/{id}")
    public ResponseEntity<Map<String, Object>> updateLesson(@PathVariable Long id,
                                                            @Valid @RequestBody AdminLessonRequest request) {
        return handleEntityAction(() -> lessonService.updateLesson(id, request), "Đã cập nhật Lesson");
    }

    @DeleteMapping("/lessons/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteLesson(@PathVariable Long id) {
        return handleAction(() -> lessonService.deleteLesson(id), "Đã xóa mềm Lesson");
    }

    @PostMapping("/sentences")
    public ResponseEntity<Map<String, Object>> createSentence(@Valid @RequestBody AdminSentenceRequest request) {
        return handleEntityAction(() -> sentenceService.createSentence(request), "Đã tạo Sentence");
    }

    @PutMapping("/sentences/{id}")
    public ResponseEntity<Map<String, Object>> updateSentence(@PathVariable Long id,
                                                              @Valid @RequestBody AdminSentenceRequest request) {
        return handleEntityAction(() -> sentenceService.updateSentence(id, request), "Đã cập nhật Sentence");
    }

    @DeleteMapping("/sentences/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteSentence(@PathVariable Long id) {
        return handleAction(() -> sentenceService.softDeleteSentence(id), "Đã xóa mềm Sentence");
    }

    @DeleteMapping("/trash/sentences/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSentence(@PathVariable Long id) {
        return handleActionThrows(() -> sentenceService.hardDeleteSentence(id), "Đã xóa vĩnh viễn Sentence");
    }

    @PatchMapping("/comments/{id}/toggle-hide")
    public ResponseEntity<Map<String, Object>> toggleHideComment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
            comment.setIsHidden(Boolean.TRUE.equals(comment.getIsHidden()) ? false : true);
            commentRepository.save(comment);
            response.put("success", true);
            response.put("hidden", comment.getIsHidden());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteComment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Comment comment = commentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
            comment.setIsDeleted(true);
            commentRepository.save(comment);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PatchMapping("/slideshows/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleSlideshowActive(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Slideshow slideshow = slideshowRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Slideshow không tồn tại"));
            slideshow.setIsActive(Boolean.TRUE.equals(slideshow.getIsActive()) ? false : true);
            slideshowRepository.save(slideshow);
            response.put("success", true);
            response.put("active", slideshow.getIsActive());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> deleteSlideshow(@PathVariable Long id) {
        return handleAction(() -> slideshowRepository.deleteById(id), "Đã xóa slideshow");
    }

    @PostMapping("/uploads")
    public ResponseEntity<Map<String, Object>> uploadAsset(@RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "resourceType", defaultValue = "auto") String resourceType,
                                                           @RequestParam(value = "folder", required = false) String folder) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File upload không được để trống.");
            }
            Map<String, String> uploadResult = cloudinaryService.uploadFile(file, resourceType, folder);
            response.put("success", true);
            response.put("message", "Tải file lên thành công.");
            response.put("data", uploadResult);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/content/categories/{categoryId}/sections")
    public ResponseEntity<List<Section>> getSectionsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(sectionService.getSectionsByCategoryId(categoryId));
    }

    @GetMapping("/content/sections/{sectionId}/lessons")
    public ResponseEntity<List<Lesson>> getLessonsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionId(sectionId));
    }

    @GetMapping("/content/lessons/{lessonId}/sentences")
    public ResponseEntity<List<Sentence>> getSentencesByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(sentenceService.getSentencesByLessonId(lessonId));
    }

    private ResponseEntity<Map<String, Object>> handleEntityAction(EntitySupplier supplier, String message) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("message", message);
            response.put("data", supplier.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleAction(Runnable action, String message) {
        Map<String, Object> response = new HashMap<>();
        try {
            action.run();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> handleActionThrows(ThrowingRunnable action, String message) {
        Map<String, Object> response = new HashMap<>();
        try {
            action.run();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface EntitySupplier {
        Object get();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        response.put("success", false);
        response.put("message", fieldError != null ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ.");
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<Map<String, Object>> handleBadRequest(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Dữ liệu gửi lên không hợp lệ.");
        return ResponseEntity.badRequest().body(response);
    }
}
