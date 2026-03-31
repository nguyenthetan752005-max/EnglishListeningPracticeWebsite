package com.english.learning.entity;

import com.english.learning.enums.Role;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
@SQLRestriction("is_deleted = false")
public class User {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    private String avatarUrl;
    private String provider;
    private String providerId;
    private Integer totalActiveTime = 0;

    // Cached values updated by Leaderboard Cron Job to ensure ultra-fast Leaderboard Retrieval
    @Column(name = "active_time_7d")
    private Integer activeTime7d = 0;

    @Column(name = "active_time_30d")
    private Integer activeTime30d = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
