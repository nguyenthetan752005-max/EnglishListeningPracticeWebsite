package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminSentenceRequest {
    @NotNull
    private Long lessonId;
    @NotBlank
    private String content;
    private String audioUrl;
    private Double startTime;
    private Double endTime;
    private Integer orderIndex = 0;
    private ContentStatus status = ContentStatus.DRAFT;
}
