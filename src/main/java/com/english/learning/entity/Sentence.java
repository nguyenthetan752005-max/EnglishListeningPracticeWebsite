package com.english.learning.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sentences", indexes = {
    @Index(name = "idx_lesson_order", columnList = "lesson_id, orderIndex")
})
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    private String audioUrl;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    private Double startTime;
    private Double endTime;
    
    private Integer orderIndex;
}