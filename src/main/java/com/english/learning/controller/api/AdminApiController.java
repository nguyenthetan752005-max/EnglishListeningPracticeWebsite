package com.english.learning.controller.api;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.dto.AdminSlideshowRequest;
import com.english.learning.entity.Comment;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.Slideshow;
import com.english.learning.repository.CommentVoteRepository;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.CategoryService;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.LessonService;
import com.english.learning.service.SectionService;
import com.english.learning.service.SlideshowService;
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
    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final SentenceRepository sentenceRepository;
    private final SlideshowService slideshowService;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteUser(@PathVariable Long id) {
        return handleAction(() -> userService.softDeleteUser(id), "Đã xóa mềm người dùng thành công");
    }

    @PostMapping("/users/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreUser(@PathVariable Long id) {
        return handleAction(() -> userService.restoreUser(id), "Đã khôi phục người dùng");
    }

    @DeleteMapping("/trash/users/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteUser(@PathVariable Long id) {
        return handleActionThrows(() -> userService.hardDeleteUser(id), "Đã xóa vĩnh viễn tài khoản và toàn bộ dữ liệu liên quan");
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

    @PostMapping("/categories/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreCategory(@PathVariable Long id) {
        return handleAction(() -> categoryService.restoreCategory(id), "Đã khôi phục Category");
    }

    @DeleteMapping("/trash/categories/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteCategory(@PathVariable Long id) {
        return handleActionThrows(() -> categoryService.hardDeleteCategory(id), "Đã xóa vĩnh viễn Category");
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

    @PostMapping("/sections/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreSection(@PathVariable Long id) {
        return handleAction(() -> sectionService.restoreSection(id), "Đã khôi phục Section");
    }

    @DeleteMapping("/trash/sections/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSection(@PathVariable Long id) {
        return handleAction(() -> sectionService.hardDeleteSection(id), "Đã xóa vĩnh viễn Section");
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

    @PostMapping("/lessons/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreLesson(@PathVariable Long id) {
        return handleAction(() -> lessonService.restoreLesson(id), "Đã khôi phục Lesson");
    }

    @DeleteMapping("/trash/lessons/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteLesson(@PathVariable Long id) {
        return handleAction(() -> lessonService.hardDeleteLesson(id), "Đã xóa vĩnh viễn Lesson");
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

    @PostMapping("/sentences/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreSentence(@PathVariable Long id) {
        return handleAction(() -> sentenceService.restoreSentence(id), "Đã khôi phục Sentence");
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
            comment.setIsHidden(true);
            commentRepository.save(comment);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/comments/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreComment(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Comment comment = commentRepository.findAnyCommentById(id)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
            Long sentenceId = comment.getSentence() != null ? comment.getSentence().getId() : null;
            Long userId = comment.getUser() != null ? comment.getUser().getId() : null;
            Sentence sentence = sentenceId != null ? sentenceRepository.findAnySentenceById(sentenceId).orElse(null) : null;
            com.english.learning.entity.User user = userId != null ? userRepository.findAnyUserById(userId).orElse(null) : null;
            if (sentence == null || Boolean.TRUE.equals(sentence.getIsDeleted())) {
                throw new RuntimeException("Không thể khôi phục Comment khi Sentence cha đang bị xóa.");
            }
            if (user == null || Boolean.TRUE.equals(user.getIsDeleted())) {
                throw new RuntimeException("Không thể khôi phục Comment khi User đã bị xóa.");
            }
            comment.setSentence(sentence);
            comment.setUser(user);
            comment.setIsDeleted(false);
            comment.setIsHidden(false);
            commentRepository.save(comment);
            response.put("success", true);
            response.put("message", "Đã khôi phục Comment");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/trash/comments/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteComment(@PathVariable Long id) {
        return handleAction(() -> hardDeleteCommentTree(id), "Đã xóa vĩnh viễn Comment");
    }

    @PostMapping("/slideshows")
    public ResponseEntity<Map<String, Object>> createSlideshow(@Valid @RequestBody AdminSlideshowRequest request) {
        return handleEntityAction(() -> slideshowService.createSlideshow(request), "Đã tạo Slideshow");
    }

    @PutMapping("/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> updateSlideshow(@PathVariable Long id,
                                                               @Valid @RequestBody AdminSlideshowRequest request) {
        return handleEntityAction(() -> slideshowService.updateSlideshow(id, request), "Đã cập nhật Slideshow");
    }

    @PatchMapping("/slideshows/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleSlideshowActive(@PathVariable Long id) {
        return handleAction(() -> slideshowService.toggleActive(id), "Đã cập nhật trạng thái slideshow");
    }

    @DeleteMapping("/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> deleteSlideshow(@PathVariable Long id) {
        return handleAction(() -> slideshowService.softDeleteSlideshow(id), "Đã xóa mềm slideshow");
    }

    @PostMapping("/slideshows/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreSlideshow(@PathVariable Long id) {
        return handleAction(() -> slideshowService.restoreSlideshow(id), "Đã khôi phục slideshow");
    }

    @DeleteMapping("/trash/slideshows/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteSlideshow(@PathVariable Long id) {
        return handleActionThrows(() -> slideshowService.hardDeleteSlideshow(id), "Đã xóa vĩnh viễn slideshow");
    }

    @PostMapping("/uploads")
    public ResponseEntity<Map<String, Object>> uploadAsset(@RequestParam("file") MultipartFile file,
                                                           @RequestParam(value = "resourceType", defaultValue = "auto") String resourceType,
                                                           @RequestParam(value = "folder", required = false) String folder,
                                                           @RequestParam(value = "publicId", required = false) String publicId,
                                                           @RequestParam(value = "overwrite", defaultValue = "false") boolean overwrite) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File upload không được để trống.");
            }
            Map<String, String> uploadResult = (publicId != null && !publicId.isBlank())
                    ? cloudinaryService.uploadFile(file, resourceType, folder, publicId, overwrite)
                    : cloudinaryService.uploadFile(file, resourceType, folder);
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

    private void hardDeleteCommentTree(Long id) {
        Comment comment = commentRepository.findAnyCommentById(id)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại"));
        for (Comment child : commentRepository.findAnyByParentId(id)) {
            hardDeleteCommentTree(child.getId());
        }
        commentVoteRepository.deleteByCommentId(comment.getId());
        commentRepository.deleteById(comment.getId());
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
