package com.english.learning.service.impl;

import com.english.learning.dto.AdminCategoryRequest;
import com.english.learning.dto.LessonDTO;
import com.english.learning.dto.SectionDTO;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.entity.Category;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.User;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.enums.PracticeType;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.CategoryService;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.LessonService;
import com.english.learning.service.SectionService;
import com.english.learning.service.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;
    private final SectionService sectionService;
    private final LessonService lessonService;
    private final UserProgressService userProgressService;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByOrderIndexAscIdAsc();
    }

    @Override
    public List<Category> getPublishedCategories() {
        return categoryRepository.findByStatusOrderByOrderIndexAscIdAsc(ContentStatus.PUBLISHED);
    }

    @Override
    public List<Category> getCategoriesByPracticeType(PracticeType practiceType) {
        return categoryRepository.findByPracticeType(practiceType);
    }

    @Override
    public List<Category> getPublishedCategoriesByPracticeType(PracticeType practiceType) {
        return categoryRepository.findByPracticeTypeAndStatusOrderByOrderIndexAscIdAsc(practiceType, ContentStatus.PUBLISHED);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> getPublishedCategoryById(Long id) {
        return categoryRepository.findByIdAndStatus(id, ContentStatus.PUBLISHED);
    }

    @Override
    public List<String> getExpandedLevels(String levelRange) {
        List<String> allLevels = List.of("A1", "A2", "B1", "B2", "C1", "C2");
        List<String> expandedLevels = new java.util.ArrayList<>();

        if (levelRange != null && levelRange.contains("-")) {
            String[] parts = levelRange.split("-");
            String start = parts[0].trim().toUpperCase();
            String end = parts[1].trim().toUpperCase();
            int startIdx = allLevels.indexOf(start);
            int endIdx = allLevels.indexOf(end);
            if (startIdx >= 0 && endIdx >= 0 && startIdx <= endIdx) {
                expandedLevels = allLevels.subList(startIdx, endIdx + 1);
            }
        } else if (levelRange != null && !levelRange.trim().isEmpty()) {
            expandedLevels = List.of(levelRange.trim().toUpperCase());
        }

        return expandedLevels;
    }

    @Override
    public List<SectionWithLessonsDTO> getSectionWithLessonsDTOs(Long categoryId, User user, PracticeType practiceType) {
        List<Section> sections = sectionService.getPublishedSectionsByCategoryId(categoryId);
        return sections.stream()
                .map(sec -> {
                    List<Lesson> filteredLessons = lessonService.getPublishedLessonsBySectionId(sec.getId()).stream()
                            .filter(l -> l.getSection().getCategory().getPracticeType() == practiceType)
                            .toList();

                    Map<Long, UserProgressStatus> lessonStatuses = new HashMap<>();
                    UserProgressStatus sectionStatus = null;

                    if (user != null) {
                        for (Lesson lesson : filteredLessons) {
                            UserProgressStatus lessonStatus = userProgressService.getLessonStatus(user.getId(), lesson.getId());
                            if (lessonStatus != null) {
                                lessonStatuses.put(lesson.getId(), lessonStatus);
                            }
                        }
                        sectionStatus = userProgressService.getSectionStatus(user.getId(), sec.getId());
                    }

                    SectionDTO sectionDto = SectionDTO.builder()
                            .id(sec.getId())
                            .categoryId(sec.getCategory() != null ? sec.getCategory().getId() : null)
                            .name(sec.getName())
                            .description(sec.getDescription())
                            .build();

                    List<LessonDTO> lessonDtos = filteredLessons.stream()
                            .map(lesson -> LessonDTO.builder()
                                    .id(lesson.getId())
                                    .sectionId(lesson.getSection() != null ? lesson.getSection().getId() : null)
                                    .type(lesson.getSection() != null && lesson.getSection().getCategory() != null
                                            ? lesson.getSection().getCategory().getType()
                                            : null)
                                    .youtubeVideoId(lesson.getYoutubeVideoId())
                                    .title(lesson.getTitle())
                                    .level(lesson.getLevel())
                                    .totalSentences(lesson.getTotalSentences())
                                    .build())
                            .toList();

                    return new SectionWithLessonsDTO(sectionDto, lessonDtos, lessonStatuses, sectionStatus);
                })
                .filter(dto -> !dto.getLessons().isEmpty())
                .toList();
    }

    @Override
    @Transactional
    public Category createCategory(AdminCategoryRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Category category = new Category();
        applyCategoryRequest(category, request);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, AdminCategoryRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        validateCategoryStatusTransition(category.getId(), request.getStatus());
        assertCategoryChanged(category, request);
        applyCategoryRequest(category, request);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));

        if (sectionRepository.countByCategory_Id(id) > 0) {
            throw new ResourceInUseException("Không thể xóa Category. Bạn phải xóa hết Section bên dưới trước.");
        }

        category.setIsDeleted(true);
        category.setStatus(ContentStatus.ARCHIVED);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void restoreCategory(Long id) {
        Category category = categoryRepository.findAnyCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        category.setIsDeleted(false);
        category.setStatus(ContentStatus.DRAFT);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findAnyCategoryById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        if (sectionRepository.countAnyByCategoryId(id) > 0) {
            throw new ResourceInUseException("Không thể xóa vĩnh viễn Category khi vẫn còn Section bên dưới. Hãy xóa cứng dữ liệu tầng dưới trước.");
        }
        if (category.getCloudImageId() != null && !category.getCloudImageId().isBlank()) {
            cloudinaryService.deleteFile(category.getCloudImageId());
        }
        categoryRepository.deleteById(id);
    }

    private void applyCategoryRequest(Category category, AdminCategoryRequest request) {
        String imageUrl = normalizeBlank(request.getImageUrl());
        String cloudImageId = normalizeBlank(request.getCloudImageId());
        replaceCloudinaryAsset(category.getCloudImageId(), cloudImageId);

        category.setName(request.getName().trim());
        category.setImageUrl(imageUrl);
        category.setCloudImageId(cloudImageId);
        category.setLevelRange(normalizeBlank(request.getLevelRange()));
        category.setType(request.getType() != null ? request.getType() : LessonType.AUDIO);
        category.setPracticeType(request.getPracticeType() != null ? request.getPracticeType() : PracticeType.LISTENING);
        category.setDescription(normalizeBlank(request.getDescription()));
        category.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        category.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
    }

    private void validateStatusForAdminMutation(ContentStatus status) {
        if (status == ContentStatus.ARCHIVED) {
            throw new IllegalArgumentException("ARCHIVED chỉ được dùng nội bộ cho thùng rác, không được chọn khi thêm hoặc sửa.");
        }
    }

    private void validateCategoryStatusTransition(Long categoryId, ContentStatus nextStatus) {
        if (nextStatus != ContentStatus.DRAFT) {
            return;
        }
        boolean hasPublishedChildren = sectionRepository.countByCategory_IdAndStatus(categoryId, ContentStatus.PUBLISHED) > 0
                || lessonRepository.countBySection_Category_IdAndStatus(categoryId, ContentStatus.PUBLISHED) > 0
                || sentenceRepository.countByLesson_Section_Category_IdAndStatus(categoryId, ContentStatus.PUBLISHED) > 0;
        if (hasPublishedChildren) {
            throw new ResourceInUseException("Không thể chuyển Category về DRAFT khi bên dưới vẫn còn dữ liệu PUBLISHED.");
        }
    }

    private void assertCategoryChanged(Category category, AdminCategoryRequest request) {
        boolean unchanged = Objects.equals(category.getName(), request.getName().trim())
                && Objects.equals(category.getImageUrl(), normalizeBlank(request.getImageUrl()))
                && Objects.equals(category.getCloudImageId(), normalizeBlank(request.getCloudImageId()))
                && Objects.equals(category.getLevelRange(), normalizeBlank(request.getLevelRange()))
                && Objects.equals(category.getType(), request.getType() != null ? request.getType() : LessonType.AUDIO)
                && Objects.equals(category.getPracticeType(), request.getPracticeType() != null ? request.getPracticeType() : PracticeType.LISTENING)
                && Objects.equals(category.getDescription(), normalizeBlank(request.getDescription()))
                && Objects.equals(category.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                && Objects.equals(category.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (unchanged) {
            throw new IllegalArgumentException("Dữ liệu chưa thay đổi.");
        }
    }

    private void replaceCloudinaryAsset(String currentPublicId, String nextPublicId) {
        if (Objects.equals(currentPublicId, nextPublicId) || currentPublicId == null || currentPublicId.isBlank()) {
            return;
        }
        try {
            cloudinaryService.deleteFile(currentPublicId);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể thay thế ảnh cũ trên Cloudinary.");
        }
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
