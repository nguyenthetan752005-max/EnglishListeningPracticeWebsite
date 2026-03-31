package com.english.learning.service.impl;

import com.english.learning.dto.AdminDashboardDTO;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.AdminDashboardService;
import com.english.learning.util.TimeFormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Implementation of AdminDashboardService.
 * Aggregates all data needed for the admin dashboard, including
 * statistics, user lists, and recycle bin items.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final SentenceRepository sentenceRepository;

    @Override
    public AdminDashboardDTO getDashboardData() {
        AdminDashboardDTO.AdminDashboardDTOBuilder builder = AdminDashboardDTO.builder();

        // Overview Stats
        try {
            builder.totalUsers(userRepository.count());
            builder.totalLessons(lessonRepository.count());
            Long totalTime = userRepository.sumTotalActiveTime();
            if (totalTime == null) totalTime = 0L;
            builder.formattedTotalTime(TimeFormatUtil.formatActiveTime(totalTime.intValue()));
        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê tổng quan: {}", e.getMessage());
            builder.totalUsers(0).totalLessons(0).formattedTotalTime("0 phút");
        }

        // Recent Users
        try {
            builder.recentUsers(userRepository.findRecentActiveUsers());
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách user gần đây: {}", e.getMessage());
            builder.recentUsers(Collections.emptyList());
        }

        // All Users
        try {
            builder.allUsers(userRepository.findAll());
        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả users: {}", e.getMessage());
            builder.allUsers(Collections.emptyList());
        }

        // All Lessons
        try {
            builder.allLessons(lessonRepository.findAll());
        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả lessons: {}", e.getMessage());
            builder.allLessons(Collections.emptyList());
        }

        // Recycle Bin (bypass @SQLRestriction via native queries)
        try {
            builder.deletedUsers(userRepository.findDeletedUsers());
        } catch (Exception e) {
            log.error("Lỗi khi lấy deleted users: {}", e.getMessage());
            builder.deletedUsers(Collections.emptyList());
        }

        try {
            builder.deletedSentences(sentenceRepository.findDeletedSentences());
        } catch (Exception e) {
            log.error("Lỗi khi lấy deleted sentences: {}", e.getMessage());
            builder.deletedSentences(Collections.emptyList());
        }

        return builder.build();
    }
}
