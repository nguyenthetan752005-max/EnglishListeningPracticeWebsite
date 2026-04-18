package com.english.learning.service.impl.mobile;

import com.english.learning.dto.mobile.MobileCategoryCollectionResponse;
import com.english.learning.dto.mobile.MobileCategoryCollectionSectionDto;
import com.english.learning.dto.mobile.MobileCategoryResponse;
import com.english.learning.dto.mobile.MobileLessonResponse;
import com.english.learning.entity.Category;
import com.english.learning.enums.ContentStatus;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.service.mobile.MobileCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileCategoryServiceImpl implements MobileCategoryService {

    private final CategoryRepository categoryRepository;
    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;
    private final MobileResponseMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MobileCategoryResponse> getAllCategories() {
        return categoryRepository.findPublishedCategories(ContentStatus.PUBLISHED)
                .stream()
                .map(mapper::toMobileCategory)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MobileCategoryCollectionResponse getCategoryWithSections(String slug) {
        Optional<Category> categoryOpt = categoryRepository.findPublishedBySlug(slug, ContentStatus.PUBLISHED);
        if (categoryOpt.isEmpty()) {
            return null;
        }

        Category category = categoryOpt.get();

        List<MobileCategoryCollectionSectionDto> sectionDtos = sectionRepository
                .findPublishedSectionsByCategoryId(category.getId(), ContentStatus.PUBLISHED)
                .stream()
                .map(section -> {
                    List<MobileLessonResponse> lessons = lessonRepository
                            .findPublishedLessonsBySectionId(section.getId(), ContentStatus.PUBLISHED)
                            .stream()
                            .map(mapper::toMobileLesson)
                            .collect(Collectors.toList());

                    return mapper.toMobileCategoryCollectionSection(section, lessons);
                })
                .collect(Collectors.toList());

        return MobileCategoryCollectionResponse.builder()
                .categoryId(category.getId())
                .categorySlug(category.getSlug())
                .categoryName(category.getName())
                .description(category.getDescription())
                .totalLessons(category.getTotalLessons())
                .sections(sectionDtos)
                .build();
    }
}
