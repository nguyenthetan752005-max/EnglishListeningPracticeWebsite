package com.english.learning.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sections")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;
    private String description;
}
