package com.english.learning.repository;

import com.english.learning.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySectionId(Long sectionId);
    List<Lesson> findBySection_Id(Long sectionId);
}
