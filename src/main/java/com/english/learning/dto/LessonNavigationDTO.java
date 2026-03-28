package com.english.learning.dto;

import com.english.learning.entity.Lesson;

public class LessonNavigationDTO {
    private Lesson nextLesson;
    private boolean isLastLessonInSection;

    public LessonNavigationDTO() {}

    public LessonNavigationDTO(Lesson nextLesson, boolean isLastLessonInSection) {
        this.nextLesson = nextLesson;
        this.isLastLessonInSection = isLastLessonInSection;
    }

    public Lesson getNextLesson() {
        return nextLesson;
    }

    public void setNextLesson(Lesson nextLesson) {
        this.nextLesson = nextLesson;
    }

    public boolean isLastLessonInSection() {
        return isLastLessonInSection;
    }

    public void setLastLessonInSection(boolean lastLessonInSection) {
        isLastLessonInSection = lastLessonInSection;
    }
}
