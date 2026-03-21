package com.english.learning.repository;

import com.english.learning.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    Optional<UserProgress> findByUser_IdAndLesson_Id(Long userId, Long lessonId);
    List<UserProgress> findByUser_Id(Long userId);
}
