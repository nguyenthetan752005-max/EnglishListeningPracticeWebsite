package com.english.learning.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String imageUrl;
    private String levelRange;

    @Enumerated(EnumType.STRING)
    private LessonType type = LessonType.AUDIO;

    @Column(nullable = false, columnDefinition = "int default 0")
    private Integer totalLessons = 0;

    private String description;
}
