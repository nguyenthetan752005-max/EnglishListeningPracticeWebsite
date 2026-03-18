package com.english.learning.model;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
@Table(name = "sentences")
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private String audioUrl;
    private String content;
    private Integer orderIndex;
}