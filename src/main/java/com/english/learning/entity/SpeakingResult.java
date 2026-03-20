package com.english.learning.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "speaking_results")
public class SpeakingResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double accuracy;
    private String recognizedText;
    private String userAudioUrl;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "sentence_id")
    private Sentence sentence;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
