package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminLessonRequest {
    @NotNull
    private Long sectionId;
    @NotBlank
    private String title;
    private String youtubeVideoId;
    private String level;
    private ContentStatus status = ContentStatus.DRAFT;
    private Integer orderIndex = 0;
}
