package com.english.learning.entity;

import org.hibernate.annotations.SQLRestriction;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.LessonType;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "lessons")
@SQLRestriction("is_deleted = false")
public class Lesson {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "order_index")
    private Integer orderIndex = 0;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;
    
    @Enumerated(EnumType.STRING)
    private LessonType type = LessonType.AUDIO;

    private String youtubeVideoId;
    private String title;
    private String level;
    private Integer totalSentences;
}
