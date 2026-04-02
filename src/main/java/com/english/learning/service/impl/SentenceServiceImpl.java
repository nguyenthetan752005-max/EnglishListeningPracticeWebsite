package com.english.learning.service.impl;

import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Sentence;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import com.english.learning.exception.ResourceInUseException;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.HintService;
import com.english.learning.service.SentenceService;
import com.english.learning.util.TextNormalizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SentenceServiceImpl implements SentenceService {

    private final SentenceRepository sentenceRepository;
    private final LessonRepository lessonRepository;
    private final HintService hintService;
    private final SpeakingResultRepository speakingResultRepository;
    private final UserProgressRepository userProgressRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        hydrateSentences(sentences);
        return sentences;
    }

    @Override
    public List<Sentence> getPublishedSentencesByLessonId(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdAndStatusOrderByOrderIndexAsc(lessonId, ContentStatus.PUBLISHED);
        hydrateSentences(sentences);
        return sentences;
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceRepository.findById(id);
    }

    @Override
    public Optional<Sentence> getPublishedSentenceById(Long id) {
        return sentenceRepository.findPublishedById(id, ContentStatus.PUBLISHED);
    }

    private void hydrateSentences(List<Sentence> sentences) {
        for (Sentence sentence : sentences) {
            String cleanText = TextNormalizerUtil.cleanHtmlAndBrackets(sentence.getContent());
            sentence.setContent(cleanText);
            sentence.setProperNouns(hintService.extractProperNouns(cleanText));
        }
    }

    @Override
    public Map<Long, List<String>> getProperNounHints(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdAndStatusOrderByOrderIndexAsc(lessonId, ContentStatus.PUBLISHED);
        return hintService.getHintsMap(sentences);
    }

    @Override
    @Transactional
    public Sentence createSentence(AdminSentenceRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Sentence sentence = new Sentence();
        applySentenceRequest(sentence, request);
        Sentence savedSentence = sentenceRepository.save(sentence);
        syncLessonSentenceCount(savedSentence.getLesson().getId());
        return savedSentence;
    }

    @Override
    @Transactional
    public Sentence updateSentence(Long id, AdminSentenceRequest request) {
        validateStatusForAdminMutation(request.getStatus());
        Sentence sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence không tồn tại."));
        Long oldLessonId = sentence.getLesson() != null ? sentence.getLesson().getId() : null;
        assertSentenceChanged(sentence, request);
        applySentenceRequest(sentence, request);
        Sentence savedSentence = sentenceRepository.save(sentence);
        syncLessonSentenceCount(oldLessonId);
        if (savedSentence.getLesson() != null && !savedSentence.getLesson().getId().equals(oldLessonId)) {
            syncLessonSentenceCount(savedSentence.getLesson().getId());
        }
        return savedSentence;
    }

    @Override
    @Transactional
    public void softDeleteSentence(Long id) {
        Sentence sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence không tồn tại."));
        sentence.setIsDeleted(true);
        sentence.setStatus(ContentStatus.ARCHIVED);
        sentenceRepository.save(sentence);
        if (sentence.getLesson() != null) {
            syncLessonSentenceCount(sentence.getLesson().getId());
        }
    }

    @Override
    @Transactional
    public void restoreSentence(Long id) {
        Sentence sentence = sentenceRepository.findAnySentenceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sentence không tồn tại."));
        Long lessonId = sentence.getLesson() != null ? sentence.getLesson().getId() : null;
        Lesson lesson = lessonId != null ? lessonRepository.findAnyLessonById(lessonId).orElse(null) : null;
        if (lesson == null || Boolean.TRUE.equals(lesson.getIsDeleted())) {
            throw new ResourceNotFoundException("Không thể khôi phục Sentence khi Lesson cha đang bị xóa.");
        }
        sentence.setLesson(lesson);
        sentence.setIsDeleted(false);
        sentence.setStatus(ContentStatus.DRAFT);
        sentenceRepository.save(sentence);
        if (sentence.getLesson() != null) {
            syncLessonSentenceCount(sentence.getLesson().getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteSentence(Long id) throws Exception {
        Sentence sentence = sentenceRepository.findAnySentenceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Câu không tồn tại."));

        if (speakingResultRepository.countBySentence_Id(id) > 0 || userProgressRepository.countBySentence_Id(id) > 0) {
            throw new ResourceInUseException("Không thể xoá cứng. Dữ liệu đang chịu ràng buộc khóa ngoại (người dùng đã làm bài hoặc lưu ghi âm).");
        }

        Long lessonId = sentence.getLesson() != null ? sentence.getLesson().getId() : null;
        if (sentence.getCloudAudioId() != null && !sentence.getCloudAudioId().isEmpty()) {
            cloudinaryService.deleteFile(sentence.getCloudAudioId());
        }

        sentenceRepository.deleteById(id);
        syncLessonSentenceCount(lessonId);
    }

    private void applySentenceRequest(Sentence sentence, AdminSentenceRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson không tồn tại"));
        validateLessonMedia(lesson, request);
        String audioUrl = normalizeAudioUrlForLesson(lesson, request.getAudioUrl());
        String cloudAudioId = normalizeCloudAudioIdForLesson(lesson, request.getCloudAudioId());
        Double startTime = normalizeStartTimeForLesson(lesson, request.getStartTime());
        Double endTime = normalizeEndTimeForLesson(lesson, request.getEndTime());
        replaceCloudinaryAudio(sentence.getCloudAudioId(), cloudAudioId);
        sentence.setLesson(lesson);
        sentence.setContent(request.getContent().trim());
        sentence.setAudioUrl(audioUrl);
        sentence.setCloudAudioId(cloudAudioId);
        sentence.setStartTime(startTime);
        sentence.setEndTime(endTime);
        sentence.setOrderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0);
        sentence.setStatus(request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
    }

    private void validateStatusForAdminMutation(ContentStatus status) {
        if (status == ContentStatus.ARCHIVED) {
            throw new IllegalArgumentException("ARCHIVED chỉ được dùng nội bộ cho thùng rác, không được chọn khi thêm hoặc sửa.");
        }
    }

    private void assertSentenceChanged(Sentence sentence, AdminSentenceRequest request) {
        Lesson lesson = lessonRepository.findById(request.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson không tồn tại"));
        validateLessonMedia(lesson, request);
        boolean unchanged = Objects.equals(sentence.getLesson() != null ? sentence.getLesson().getId() : null, request.getLessonId())
                && Objects.equals(sentence.getContent(), request.getContent().trim())
                && Objects.equals(sentence.getAudioUrl(), normalizeAudioUrlForLesson(lesson, request.getAudioUrl()))
                && Objects.equals(sentence.getCloudAudioId(), normalizeCloudAudioIdForLesson(lesson, request.getCloudAudioId()))
                && Objects.equals(sentence.getStartTime(), normalizeStartTimeForLesson(lesson, request.getStartTime()))
                && Objects.equals(sentence.getEndTime(), normalizeEndTimeForLesson(lesson, request.getEndTime()))
                && Objects.equals(sentence.getOrderIndex(), request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                && Objects.equals(sentence.getStatus(), request.getStatus() != null ? request.getStatus() : ContentStatus.DRAFT);
        if (unchanged) {
            throw new IllegalArgumentException("Dữ liệu chưa thay đổi.");
        }
    }

    private void validateLessonMedia(Lesson lesson, AdminSentenceRequest request) {
        LessonType lessonType = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getType()
                : LessonType.AUDIO;
        if (lessonType == LessonType.VIDEO) {
            String youtubeVideoId = normalizeBlank(lesson.getYoutubeVideoId());
            if (youtubeVideoId == null || youtubeVideoId.length() != 11) {
                throw new IllegalArgumentException("Lesson video đang có link YouTube không hợp lệ.");
            }
            if (request.getStartTime() == null || request.getEndTime() == null) {
                throw new IllegalArgumentException("Sentence video phải có start time và end time.");
            }
            if (request.getEndTime() <= request.getStartTime()) {
                throw new IllegalArgumentException("End time phải lớn hơn start time.");
            }
            return;
        }
        if (normalizeBlank(request.getAudioUrl()) == null) {
            throw new IllegalArgumentException("Sentence audio phải có audio URL hoặc audio upload.");
        }
    }

    private String normalizeAudioUrlForLesson(Lesson lesson, String audioUrl) {
        LessonType lessonType = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getType()
                : LessonType.AUDIO;
        return lessonType == LessonType.AUDIO ? normalizeBlank(audioUrl) : null;
    }

    private String normalizeCloudAudioIdForLesson(Lesson lesson, String cloudAudioId) {
        LessonType lessonType = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getType()
                : LessonType.AUDIO;
        return lessonType == LessonType.AUDIO ? normalizeBlank(cloudAudioId) : null;
    }

    private Double normalizeStartTimeForLesson(Lesson lesson, Double startTime) {
        LessonType lessonType = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getType()
                : LessonType.AUDIO;
        return lessonType == LessonType.VIDEO ? startTime : null;
    }

    private Double normalizeEndTimeForLesson(Lesson lesson, Double endTime) {
        LessonType lessonType = lesson.getSection() != null && lesson.getSection().getCategory() != null
                ? lesson.getSection().getCategory().getType()
                : LessonType.AUDIO;
        return lessonType == LessonType.VIDEO ? endTime : null;
    }

    private void replaceCloudinaryAudio(String currentPublicId, String nextPublicId) {
        if (Objects.equals(currentPublicId, nextPublicId)
                || currentPublicId == null
                || currentPublicId.isBlank()
                || nextPublicId == null
                || nextPublicId.isBlank()) {
            return;
        }
        try {
            cloudinaryService.deleteFile(currentPublicId);
        } catch (Exception e) {
            throw new IllegalStateException("Không thể thay thế audio cũ trên Cloudinary.");
        }
    }

    private void syncLessonSentenceCount(Long lessonId) {
        if (lessonId == null) {
            return;
        }
        lessonRepository.findById(lessonId).ifPresent(lesson -> {
            lesson.setTotalSentences((int) sentenceRepository.countByLesson_Id(lessonId));
            lessonRepository.save(lesson);
        });
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
