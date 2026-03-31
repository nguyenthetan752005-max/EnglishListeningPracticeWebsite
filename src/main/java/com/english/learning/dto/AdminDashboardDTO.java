package com.english.learning.dto;

import com.english.learning.entity.Lesson;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO aggregating all data needed to render the Admin Dashboard.
 * Encapsulates statistics, user lists, and recycle bin data.
 */
@Data
@Builder
public class AdminDashboardDTO {
    private long totalUsers;
    private long totalLessons;
    private String formattedTotalTime;

    private List<User> recentUsers;
    private List<User> allUsers;
    private List<Lesson> allLessons;

    // Recycle Bin
    private List<User> deletedUsers;
    private List<Sentence> deletedSentences;
}
