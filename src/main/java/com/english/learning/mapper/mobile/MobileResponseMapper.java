package com.english.learning.mapper.mobile;

import com.english.learning.dto.mobile.*;
import com.english.learning.entity.*;
import com.english.learning.enums.LessonType;
import com.english.learning.enums.Role;
import com.english.learning.repository.CommentVoteRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.util.AudioUrlResolver;
import com.english.learning.util.MobileApiUrlResolver;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Mapper: Entity -> Mobile DTO.
 * Handles all field name conversion (snake_case DB -> camelCase JSON)
 * and derived field computation (displayType, avatarLabel, timeAgo, etc.)
 */
@Component
@RequiredArgsConstructor
public class MobileResponseMapper {

    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;
    private final MobileApiUrlResolver mobileApiUrlResolver;
    private final AudioUrlResolver audioUrlResolver;

    // ===== Category =====
    public MobileCategoryResponse toMobileCategory(Category entity) {
        return MobileCategoryResponse.builder()
                .id(entity.getId())
                .slug(entity.getSlug())
                .name(entity.getName())
                .imageUrl(entity.getImageUrl())
                .levelRange(entity.getLevelRange())
                .contentType(entity.getType() != null ? entity.getType().name() : "AUDIO")
                .practiceType(entity.getPracticeType() != null ? entity.getPracticeType().name() : "LISTENING")
                .totalLessons(entity.getTotalLessons())
                .description(entity.getDescription())
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    // ===== Section =====
    public MobileSectionResponse toMobileSection(Section entity) {
        return MobileSectionResponse.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    public MobileCategoryCollectionSectionDto toMobileCategoryCollectionSection(Section entity, List<MobileLessonResponse> lessons) {
        return MobileCategoryCollectionSectionDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .orderIndex(entity.getOrderIndex())
                .lessons(lessons)
                .build();
    }

    // ===== Lesson =====
    public MobileLessonResponse toMobileLesson(Lesson entity) {
        // Derive contentType: prefer lesson.contentType, fallback to category.type
        String contentType = deriveContentType(entity);
        String displayType = deriveDisplayType(contentType);

        return MobileLessonResponse.builder()
                .id(entity.getId())
                .sectionId(entity.getSection() != null ? entity.getSection().getId() : null)
                .title(entity.getTitle())
                .level(entity.getLevel())
                .displayType(displayType)
                .contentType(contentType)
                .totalSentences(entity.getTotalSentences())
                .passThreshold(entity.getPassThreshold() != null ? entity.getPassThreshold() : 70)
                .youtubeVideoId(entity.getYoutubeVideoId())
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    // ===== Sentence =====
    public MobileSentenceResponse toMobileSentence(Sentence entity) {
        String proxyAudioUrl = null;
        String resolvedUrl = audioUrlResolver.resolve(entity);
        if (resolvedUrl != null) {
            proxyAudioUrl = mobileApiUrlResolver.buildSentenceAudioUrl(entity.getId());
        }

        return MobileSentenceResponse.builder()
                .id(entity.getId())
                .lessonId(entity.getLesson() != null ? entity.getLesson().getId() : null)
                .audioUrl(proxyAudioUrl)
                .content(entity.getContent())
                .hintText(entity.getHintText() != null ? entity.getHintText() : "")
                .durationMillis(entity.getDurationMillis())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    // ===== Comment =====
    public MobileCommentResponse toMobileComment(Comment entity) {
        String author = "Unknown";
        String avatarLabel = "?";
        if (entity.getUser() != null) {
            author = entity.getUser().getUsername();
            avatarLabel = author.substring(0, 1).toUpperCase();
        }

        long likeCount = commentVoteRepository.countVotes(entity.getId(), true);
        long dislikeCount = commentVoteRepository.countVotes(entity.getId(), false);

        return MobileCommentResponse.builder()
                .id(entity.getId())
                .sentenceId(entity.getSentence() != null ? entity.getSentence().getId() : null)
                .parentCommentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .author(author)
                .avatarLabel(avatarLabel)
                .timeAgo(deriveTimeAgo(entity.getCreatedAt()))
                .content(entity.getContent())
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .orderIndex(entity.getOrderIndex())
                .build();
    }

    // ===== Leaderboard =====
    public MobileLeaderboardResponse buildLeaderboard() {
        List<User> weekly = userRepository.findTop30ByRoleOrderByActiveTime7dDesc(Role.USER);
        List<User> monthly = userRepository.findTop30ByRoleOrderByActiveTime30dDesc(Role.USER);

        List<MobileLeaderboardEntryResponse> weeklyEntries = buildEntries(weekly, true);
        List<MobileLeaderboardEntryResponse> monthlyEntries = buildEntries(monthly, false);

        return MobileLeaderboardResponse.builder()
                .weeklyEntries(weeklyEntries)
                .monthlyEntries(monthlyEntries)
                .currentUserRank(0)
                .currentUserTime("0m")
                .build();
    }

    // ===== Private helpers =====

    private List<MobileLeaderboardEntryResponse> buildEntries(List<User> users, boolean is7Day) {
        List<MobileLeaderboardEntryResponse> entries = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            int seconds = is7Day
                    ? (u.getActiveTime7d() != null ? u.getActiveTime7d() : 0)
                    : (u.getActiveTime30d() != null ? u.getActiveTime30d() : 0);

            entries.add(MobileLeaderboardEntryResponse.builder()
                    .rank(i + 1)
                    .username(u.getUsername())
                    .initial(u.getUsername().substring(0, 1).toUpperCase())
                    .activeTime(formatActiveTimeForMobile(seconds))
                    .build());
        }
        return entries;
    }

    private String deriveContentType(Lesson lesson) {
        if (lesson.getContentType() != null) {
            return lesson.getContentType().name();
        }
        // Fallback: derive from category.type through section
        if (lesson.getSection() != null && lesson.getSection().getCategory() != null) {
            LessonType catType = lesson.getSection().getCategory().getType();
            if (catType != null) {
                return catType.name();
            }
        }
        return "AUDIO";
    }

    private String deriveDisplayType(String contentType) {
        if ("VIDEO".equals(contentType)) {
            return "Video";
        }
        return "Audio";
    }

    private String deriveTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) {
            return "just now";
        }
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) return "just now";
        if (seconds < 3600) return (seconds / 60) + " minutes ago";
        if (seconds < 86400) return (seconds / 3600) + " hours ago";
        if (seconds < 2592000) return (seconds / 86400) + " days ago";
        if (seconds < 31536000) return (seconds / 2592000) + " months ago";
        return (seconds / 31536000) + " years ago";
    }

    private String formatActiveTimeForMobile(int totalSeconds) {
        int minutes = totalSeconds / 60;
        if (minutes < 1) return "0m";
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours > 0) {
            return hours + "h " + remainingMinutes + "m";
        }
        return remainingMinutes + "m";
    }
}
