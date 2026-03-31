package com.english.learning.dto;

import com.english.learning.entity.Lesson;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO aggregating all data needed to render the Admin Dashboard.
 * Separates regular users from admin accounts for independent management.
 */
@Data
@Builder
public class AdminDashboardDTO {
    // Overview Stats
    private long totalUsers;
    private long totalAdmins;
    private long totalLessons;
    private String formattedTotalTime;

    // Recent Users (overview tab)
    private List<User> recentUsers;

    // User Management (role=USER only)
    private List<User> regularUsers;

    // Admin Management (role=ADMIN only)
    private List<User> adminUsers;

    // Lessons
    private List<Lesson> allLessons;

    // Recycle Bin
    private List<User> deletedUsers;
    private List<Sentence> deletedSentences;
}
