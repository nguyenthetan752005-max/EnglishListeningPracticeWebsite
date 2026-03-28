package com.english.learning.dto;

public class InProgressLessonDTO {
    private Long lessonId;
    private String lessonTitle;
    private String categoryName;
    private String sectionName;
    private String practiceType; // LISTENING or SPEAKING
    private int totalSentences;
    private int completedSentences;
    private int progressPercent;
    private Long firstUncompletedSentenceId;
    private int firstUncompletedSentenceIndex;

    public InProgressLessonDTO() {}

    public InProgressLessonDTO(Long lessonId, String lessonTitle, String categoryName, 
                               String sectionName, String practiceType, int totalSentences,
                               int completedSentences, Long firstUncompletedSentenceId,
                               int firstUncompletedSentenceIndex) {
        this.lessonId = lessonId;
        this.lessonTitle = lessonTitle;
        this.categoryName = categoryName;
        this.sectionName = sectionName;
        this.practiceType = practiceType;
        this.totalSentences = totalSentences;
        this.completedSentences = completedSentences;
        this.progressPercent = totalSentences > 0 ? (completedSentences * 100 / totalSentences) : 0;
        this.firstUncompletedSentenceId = firstUncompletedSentenceId;
        this.firstUncompletedSentenceIndex = firstUncompletedSentenceIndex;
    }

    // Getters and Setters
    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public String getLessonTitle() { return lessonTitle; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSectionName() { return sectionName; }
    public void setSectionName(String sectionName) { this.sectionName = sectionName; }

    public String getPracticeType() { return practiceType; }
    public void setPracticeType(String practiceType) { this.practiceType = practiceType; }

    public int getTotalSentences() { return totalSentences; }
    public void setTotalSentences(int totalSentences) { this.totalSentences = totalSentences; }

    public int getCompletedSentences() { return completedSentences; }
    public void setCompletedSentences(int completedSentences) { 
        this.completedSentences = completedSentences;
        this.progressPercent = totalSentences > 0 ? (completedSentences * 100 / totalSentences) : 0;
    }

    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }

    public Long getFirstUncompletedSentenceId() { return firstUncompletedSentenceId; }
    public void setFirstUncompletedSentenceId(Long firstUncompletedSentenceId) { 
        this.firstUncompletedSentenceId = firstUncompletedSentenceId; 
    }

    public int getFirstUncompletedSentenceIndex() { return firstUncompletedSentenceIndex; }
    public void setFirstUncompletedSentenceIndex(int firstUncompletedSentenceIndex) { 
        this.firstUncompletedSentenceIndex = firstUncompletedSentenceIndex; 
    }
}
