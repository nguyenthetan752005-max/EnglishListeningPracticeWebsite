package com.english.learning.dto;

import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.enums.UserProgressStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class SectionWithLessonsDTO {
    private Section section;
    private List<Lesson> lessons;
    private Map<Long, UserProgressStatus> lessonStatuses;
    private UserProgressStatus sectionStatus;
}
