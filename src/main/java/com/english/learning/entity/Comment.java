package com.english.learning.entity;

import com.english.learning.enums.CommentType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "sentence_id")
    @JsonIgnoreProperties({"lesson", "properNouns", "hibernateLazyInitializer"})
    private Sentence sentence;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"password", "provider", "providerId", "hibernateLazyInitializer"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties({"parent", "sentence", "hibernateLazyInitializer"})
    private Comment parent;

    @Transient
    private long likeCount;

    @Transient
    private long dislikeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50) default 'LISTENING'")
    private CommentType commentType;
}
