<<<<<<< HEAD:src/main/java/com/english/learning/model/Section.java
package com.english.learning.model;
=======
package com.english.learning.entity;
>>>>>>> origin/main:src/main/java/com/english/learning/entity/Section.java

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sections")
public class Section {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD:src/main/java/com/english/learning/model/Section.java
    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
=======
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;
    private String description;
>>>>>>> origin/main:src/main/java/com/english/learning/entity/Section.java
}
