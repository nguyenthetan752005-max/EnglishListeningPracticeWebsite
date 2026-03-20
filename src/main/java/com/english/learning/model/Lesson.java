package com.english.learning.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String level;
    private Integer totalSentences;

    @ManyToOne
    @JoinColumn(name = "section_id")
    private Section section;
}