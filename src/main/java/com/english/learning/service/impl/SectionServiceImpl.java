package com.english.learning.service.impl;

import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.entity.Category;
import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final CategoryRepository categoryRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<Section> getSectionsByCategoryId(Long categoryId) {
        return sectionRepository.findByCategory_IdOrderByOrderIndexAscIdAsc(categoryId);
    }

    @Override
    public List<Section> getPublishedSectionsByCategoryId(Long categoryId) {
        return sectionRepository.findByCategory_IdAndStatusOrderByOrderIndexAscIdAsc(categoryId, ContentStatus.PUBLISHED);
    }

    @Override
    public Optional<Section> getSectionById(Long id) {
        return sectionRepository.findById(id);
    }

    @Override
    public Optional<Section> getPublishedSectionById(Long categoryId, Long sectionId) {
        return sectionRepository.findPublishedByIdAndCategoryId(sectionId, categoryId, ContentStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public Section createSection(AdminSectionRequest request) {
        Section section = new Section();
        applySectionRequest(section, request);
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    public Section updateSection(Long id, AdminSectionRequest request) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section không tồn tại"));
        assertSectionChanged(section, request);
        applySectionRequest(section, request);
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section không tồn tại"));

        if (lessonRepository.countBySection_Id(id) > 0) {
            throw new ResourceInUseException("Không thể xóa Section. Bạn phải xóa hết Lesson bên dưới trước.");
        }

        section.setIsDeleted(true);
        sectionRepository.save(section);
    }

    private void applySectionRequest(Section section, AdminSectionRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category không tồn tại"));
        section.setCategory(category);
        section.setName(request.getName().trim());
        section.setDescription(normalizeBlank(request.getDescription()));
        section.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        section.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
    }

    private void assertSectionChanged(Section section, AdminSectionRequest request) {
        boolean unchanged = Objects.equals(section.getCategory() != null ? section.getCategory().getId() : null, request.getCategoryId())
                && Objects.equals(section.getName(), request.getName().trim())
                && Objects.equals(section.getDescription(), normalizeBlank(request.getDescription()))
                && Objects.equals(section.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                && Objects.equals(section.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (unchanged) {
            throw new IllegalArgumentException("Dữ liệu chưa thay đổi.");
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
