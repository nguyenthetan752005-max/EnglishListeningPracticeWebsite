package com.english.learning.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "lessons")
public class Lesson {
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
