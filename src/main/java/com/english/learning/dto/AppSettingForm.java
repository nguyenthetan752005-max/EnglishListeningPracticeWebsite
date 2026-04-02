package com.english.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppSettingForm {

    @NotBlank(message = "Site name is required.")
    @Size(max = 150, message = "Site name must be at most 150 characters.")
    private String siteName;

    @NotBlank(message = "SEO meta description is required.")
    @Size(max = 500, message = "SEO meta description must be at most 500 characters.")
    private String seoMetaDescription;

    @NotNull(message = "Max recent users is required.")
    @Min(value = 1, message = "Max recent users must be at least 1.")
    @Max(value = 100, message = "Max recent users must be at most 100.")
    private Integer maxRecentUsersOnDashboard;

    @NotNull(message = "Speaking pass threshold is required.")
    @Min(value = 0, message = "Speaking pass threshold must be between 0 and 100.")
    @Max(value = 100, message = "Speaking pass threshold must be between 0 and 100.")
    private Integer speakingPassThreshold;

    private boolean allowUserRegistration;
}
