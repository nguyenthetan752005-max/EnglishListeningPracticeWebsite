package com.english.learning.service.impl;

import com.english.learning.repository.CategoryRepository;
import com.english.learning.entity.Category;
import com.english.learning.service.CategoryService;
import com.english.learning.enums.PracticeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.english.learning.service.SectionService;
import com.english.learning.service.LessonService;
import com.english.learning.service.UserProgressService;
import com.english.learning.dto.SectionWithLessonsDTO;
import com.english.learning.dto.SectionDTO;
import com.english.learning.dto.LessonDTO;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.entity.User;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final SectionService sectionService;
    private final LessonService lessonService;
    private final UserProgressService userProgressService;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getCategoriesByPracticeType(PracticeType practiceType) {
        return categoryRepository.findByPracticeType(practiceType);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<String> getExpandedLevels(String levelRange) {
        List<String> ALL_LEVELS = List.of("A1", "A2", "B1", "B2", "C1", "C2");
        List<String> expandedLevels = new java.util.ArrayList<>();
        
        if (levelRange != null && levelRange.contains("-")) {
            String[] parts = levelRange.split("-");
            String start = parts[0].trim().toUpperCase();
            String end = parts[1].trim().toUpperCase();
            int startIdx = ALL_LEVELS.indexOf(start);
            int endIdx = ALL_LEVELS.indexOf(end);
            if (startIdx >= 0 && endIdx >= 0 && startIdx <= endIdx) {
                expandedLevels = ALL_LEVELS.subList(startIdx, endIdx + 1);
            }
        } else if (levelRange != null && !levelRange.trim().isEmpty()) {
            // Single level like "OET" or "A1"
            expandedLevels = List.of(levelRange.trim().toUpperCase());
        }
        
        return expandedLevels;
    }

    @Override
    public List<SectionWithLessonsDTO> getSectionWithLessonsDTOs(Long categoryId, User user, PracticeType practiceType) {
        List<Section> sections = sectionService.getSectionsByCategoryId(categoryId);
        return sections.stream()
                .map(sec -> {
                    List<Lesson> filteredLessons = lessonService.getLessonsBySectionId(sec.getId()).stream()
                            .filter(l -> l.getSection().getCategory().getPracticeType() == practiceType)
                            .toList();

                    Map<Long, UserProgressStatus> lessonStatuses = new HashMap<>();
                    UserProgressStatus sectionStatus = null;

                    if (user != null) {
                        for (Lesson l : filteredLessons) {
                            UserProgressStatus lStatus = userProgressService.getLessonStatus(user.getId(), l.getId());
                            if (lStatus != null) {
                                lessonStatuses.put(l.getId(), lStatus);
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
                        .map(l -> LessonDTO.builder()
                            .id(l.getId())
                            .sectionId(l.getSection() != null ? l.getSection().getId() : null)
                            .type(l.getType())
                            .youtubeVideoId(l.getYoutubeVideoId())
                            .title(l.getTitle())
                            .level(l.getLevel())
                            .totalSentences(l.getTotalSentences())
                            .build())
                        .toList();

                    return new SectionWithLessonsDTO(sectionDto, lessonDtos, lessonStatuses, sectionStatus);
                })
                .filter(dto -> !dto.getLessons().isEmpty())
                .toList();
    }
}
