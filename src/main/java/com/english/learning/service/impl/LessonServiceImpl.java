package com.english.learning.service.impl;

import com.english.learning.dto.AdminLessonRequest;
import com.english.learning.dto.LessonDTO;
import com.english.learning.dto.LessonNavigationDTO;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.enums.PracticeType;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{11}$");
    private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
            "^(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([A-Za-z0-9_-]{11}).*$",
            Pattern.CASE_INSENSITIVE
    );

    private final LessonRepository lessonRepository;
    private final SectionRepository sectionRepository;
    private final SentenceRepository sentenceRepository;

    @Override
    public List<Lesson> getLessonsBySectionId(Long sectionId) {
        return lessonRepository.findBySection_IdOrderByOrderIndexAscIdAsc(sectionId);
    }

    @Override
    public List<Lesson> getPublishedLessonsBySectionId(Long sectionId) {
        return lessonRepository.findBySection_IdAndStatusOrderByOrderIndexAscIdAsc(sectionId, ContentStatus.PUBLISHED);
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }

    @Override
    public Optional<Lesson> getPublishedLessonById(Long id) {
        return lessonRepository.findPublishedById(id, ContentStatus.PUBLISHED);
    }

    @Override
    @Transactional
    public Lesson createLesson(AdminLessonRequest request) {
        Lesson lesson = new Lesson();
        applyLessonRequest(lesson, request);
        Lesson savedLesson = lessonRepository.save(lesson);
        syncCategoryLessonCount(savedLesson.getSection());
        return savedLesson;
    }

    @Override
    @Transactional
    public Lesson updateLesson(Long id, AdminLessonRequest request) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson không tồn tại"));
        Long oldCategoryId = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getId()
                : null;
        assertLessonChanged(lesson, request);
        applyLessonRequest(lesson, request);
        Lesson savedLesson = lessonRepository.save(lesson);
        syncCategoryLessonCount(oldCategoryId);
        Long newCategoryId = savedLesson.getSection() != null && savedLesson.getSection().getCategory() != null
                ? savedLesson.getSection().getCategory().getId()
                : null;
        if (newCategoryId != null && !newCategoryId.equals(oldCategoryId)) {
            syncCategoryLessonCount(newCategoryId);
        }
        return savedLesson;
    }

    @Override
    @Transactional
    public void deleteLesson(Long id) {
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson không tồn tại"));
        if (sentenceRepository.countByLesson_Id(id) > 0) {
            throw new ResourceInUseException("Không thể xóa Lesson. Bạn phải xóa hết Sentence bên dưới trước.");
        }
        lesson.setIsDeleted(true);
        lessonRepository.save(lesson);
        syncCategoryLessonCount(lesson.getSection());
    }

    @Override
    public LessonNavigationDTO getLessonNavigation(Lesson currentLesson, PracticeType practiceType) {
        Section section = currentLesson.getSection();
        Lesson nextLesson = null;
        boolean isLastLessonInSection = false;

        if (section != null) {
            List<Lesson> sectionLessons = getPublishedLessonsBySectionId(section.getId()).stream()
                    .filter(l -> l.getSection().getCategory().getPracticeType() == practiceType)
                    .sorted(Comparator.comparing(Lesson::getId))
                    .toList();

            int currentIndex = -1;
            for (int i = 0; i < sectionLessons.size(); i++) {
                if (sectionLessons.get(i).getId().equals(currentLesson.getId())) {
                    currentIndex = i;
                    break;
                }
            }

            if (currentIndex >= 0 && currentIndex < sectionLessons.size() - 1) {
                nextLesson = sectionLessons.get(currentIndex + 1);
            } else if (currentIndex == sectionLessons.size() - 1) {
                isLastLessonInSection = true;
            }
        }

        LessonDTO nextLessonDto = null;
        if (nextLesson != null) {
            nextLessonDto = LessonDTO.builder()
                    .id(nextLesson.getId())
                    .sectionId(nextLesson.getSection() != null ? nextLesson.getSection().getId() : null)
                    .type(nextLesson.getSection() != null && nextLesson.getSection().getCategory() != null
                            ? nextLesson.getSection().getCategory().getType()
                            : null)
                    .youtubeVideoId(nextLesson.getYoutubeVideoId())
                    .title(nextLesson.getTitle())
                    .level(nextLesson.getLevel())
                    .totalSentences(nextLesson.getTotalSentences())
                    .build();
        }
        return new LessonNavigationDTO(nextLessonDto, isLastLessonInSection);
    }

    private void applyLessonRequest(Lesson lesson, AdminLessonRequest request) {
        Section section = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section không tồn tại"));
        String normalizedYoutubeValue = normalizeYoutubeReference(section, request.getYoutubeVideoId());
        lesson.setSection(section);
        lesson.setTitle(request.getTitle().trim());
        lesson.setYoutubeVideoId(normalizedYoutubeValue);
        lesson.setLevel(normalizeBlank(request.getLevel()));
        lesson.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        lesson.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (lesson.getTotalSentences() == null) {
            lesson.setTotalSentences(0);
        }
    }

    private void assertLessonChanged(Lesson lesson, AdminLessonRequest request) {
        Section targetSection = sectionRepository.findById(request.getSectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section không tồn tại"));
        String normalizedYoutubeValue = normalizeYoutubeReference(targetSection, request.getYoutubeVideoId());
        boolean unchanged = Objects.equals(lesson.getSection() != null ? lesson.getSection().getId() : null, request.getSectionId())
                && Objects.equals(lesson.getTitle(), request.getTitle().trim())
                && Objects.equals(lesson.getYoutubeVideoId(), normalizedYoutubeValue)
                && Objects.equals(lesson.getLevel(), normalizeBlank(request.getLevel()))
                && Objects.equals(lesson.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT)
                && Objects.equals(lesson.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        if (unchanged) {
            throw new IllegalArgumentException("Dữ liệu chưa thay đổi.");
        }
    }

    private String normalizeYoutubeReference(Section section, String rawValue) {
        String normalized = normalizeBlank(rawValue);
        LessonType lessonType = section.getCategory() != null ? section.getCategory().getType() : LessonType.AUDIO;
        if (lessonType != LessonType.VIDEO) {
            return normalized;
        }
        if (normalized == null) {
            throw new IllegalArgumentException("Link YouTube không được để trống với bài học video.");
        }
        if (YOUTUBE_ID_PATTERN.matcher(normalized).matches()) {
            return normalized;
        }
        Matcher matcher = YOUTUBE_URL_PATTERN.matcher(normalized);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Link YouTube không hợp lệ.");
    }

    private void syncCategoryLessonCount(Section section) {
        if (section == null || section.getCategory() == null) {
            return;
        }
        syncCategoryLessonCount(section.getCategory().getId());
    }

    private void syncCategoryLessonCount(Long categoryId) {
        if (categoryId == null) {
            return;
        }
        sectionRepository.findByCategory_Id(categoryId).stream()
                .findFirst()
                .map(Section::getCategory)
                .ifPresent(category -> category.setTotalLessons((int) lessonRepository.countBySection_Category_Id(categoryId)));
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
