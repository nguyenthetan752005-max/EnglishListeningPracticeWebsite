package com.english.learning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
public class AppSetting {

    public static final long SINGLETON_ID = 1L;
    public static final int DEFAULT_MAX_RECENT_USERS = 10;
    public static final int DEFAULT_SPEAKING_PASS_THRESHOLD = 70;
    public static final String DEFAULT_SITE_NAME = "English Listening Practice";
    public static final String DEFAULT_SEO_META_DESCRIPTION =
            "Free English listening and speaking practice platform with real-world conversations.";

    @Id
    private Long id = SINGLETON_ID;

    @Column(nullable = false, length = 150)
    private String siteName = DEFAULT_SITE_NAME;

    @Column(nullable = false, length = 500)
    private String seoMetaDescription = DEFAULT_SEO_META_DESCRIPTION;

    @Column(nullable = false)
    private Integer maxRecentUsersOnDashboard = DEFAULT_MAX_RECENT_USERS;

    @Column(nullable = false)
    private Integer speakingPassThreshold = DEFAULT_SPEAKING_PASS_THRESHOLD;

    @Column(nullable = false)
    private Boolean allowUserRegistration = true;
}
