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
 * separate lists for regular users vs admin accounts.
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
            builder.totalAdmins(userRepository.countAdmins());
            builder.totalLessons(lessonRepository.count());
            Long totalTime = userRepository.sumTotalActiveTime();
            if (totalTime == null) totalTime = 0L;
            builder.formattedTotalTime(TimeFormatUtil.formatActiveTime(totalTime.intValue()));
        } catch (Exception e) {
            log.error("Error fetching overview stats: {}", e.getMessage());
            builder.totalUsers(0).totalAdmins(0).totalLessons(0).formattedTotalTime("0 min");
        }

        // Recent Users (overview)
        try {
            builder.recentUsers(userRepository.findRecentActiveUsers());
        } catch (Exception e) {
            log.error("Error fetching recent users: {}", e.getMessage());
            builder.recentUsers(Collections.emptyList());
        }

        // Regular Users (role=USER)
        try {
            builder.regularUsers(userRepository.findAllRegularUsers());
        } catch (Exception e) {
            log.error("Error fetching regular users: {}", e.getMessage());
            builder.regularUsers(Collections.emptyList());
        }

        // Admin Users (role=ADMIN)
        try {
            builder.adminUsers(userRepository.findAllAdmins());
        } catch (Exception e) {
            log.error("Error fetching admin users: {}", e.getMessage());
            builder.adminUsers(Collections.emptyList());
        }

        // All Lessons
        try {
            builder.allLessons(lessonRepository.findAll());
        } catch (Exception e) {
            log.error("Error fetching lessons: {}", e.getMessage());
            builder.allLessons(Collections.emptyList());
        }

        // Recycle Bin
        try {
            builder.deletedUsers(userRepository.findDeletedUsers());
        } catch (Exception e) {
            log.error("Error fetching deleted users: {}", e.getMessage());
            builder.deletedUsers(Collections.emptyList());
        }

        try {
            builder.deletedSentences(sentenceRepository.findDeletedSentences());
        } catch (Exception e) {
            log.error("Error fetching deleted sentences: {}", e.getMessage());
            builder.deletedSentences(Collections.emptyList());
        }

        return builder.build();
    }
}
