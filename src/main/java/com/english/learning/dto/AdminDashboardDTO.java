package com.english.learning.dto;

import com.english.learning.entity.Category;
import com.english.learning.entity.Comment;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.Slideshow;
import com.english.learning.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

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
    private Map<Long, Integer> userTopScoreMap;

    // Admin Management (role=ADMIN only)
    private List<User> adminUsers;

    // Content hierarchy
    private List<Category> categories;
    private List<Section> sections;
    private List<Lesson> allLessons;
    private List<Sentence> allSentences;

    // Recycle Bin
    private List<User> deletedUsers;
    private List<Sentence> deletedSentences;

    // Comments for moderation
    private List<Comment> recentComments;

    // Slideshows (e.g. homepage banners)
    private List<Slideshow> slideshows;
}
