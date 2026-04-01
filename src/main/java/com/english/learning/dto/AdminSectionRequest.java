package com.english.learning.dto;

import com.english.learning.enums.ContentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminSectionRequest {
    @NotNull
    private Long categoryId;
    @NotBlank
    private String name;
    private String description;
    private ContentStatus status = ContentStatus.DRAFT;
    private Integer orderIndex = 0;
}
