package com.english.learning.dto;

import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SectionWithLessonsDTO {
    private Section section;
    private List<Lesson> lessons;
}
