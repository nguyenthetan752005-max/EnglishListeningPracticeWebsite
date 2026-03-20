<<<<<<< HEAD:src/main/java/com/english/learning/model/User.java
package com.english.learning.model;
=======
package com.english.learning.entity;
>>>>>>> origin/main:src/main/java/com/english/learning/entity/User.java

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

<<<<<<< HEAD:src/main/java/com/english/learning/model/User.java
    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String role = "USER";

    private String avatarUrl;
    private String provider;
    private String providerId;
    private Integer totalActiveTime = 0;
=======
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role; // ROLE_USER, ROLE_ADMIN
>>>>>>> origin/main:src/main/java/com/english/learning/entity/User.java

    @CreationTimestamp
    private LocalDateTime createdAt;
}
