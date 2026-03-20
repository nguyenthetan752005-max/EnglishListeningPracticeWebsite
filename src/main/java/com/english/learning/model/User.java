package com.english.learning.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String role = "USER";

    private String avatarUrl;
    private String provider;
    private String providerId;
    private Integer totalActiveTime = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
