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

<<<<<<< HEAD:src/main/java/com/english/learning/model/Lesson.java
=======
    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;

>>>>>>> origin/main:src/main/java/com/english/learning/entity/Lesson.java
    private String title;
    private String level;
    private Integer totalSentences;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;
}