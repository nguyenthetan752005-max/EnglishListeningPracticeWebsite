package com.english.learning.repository;

import com.english.learning.entity.Lesson;
import com.english.learning.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findBySectionId(Long sectionId);
    List<Lesson> findBySection_Id(Long sectionId);
    List<Lesson> findBySection_IdOrderByOrderIndexAscIdAsc(Long sectionId);
    List<Lesson> findBySection_IdAndStatusOrderByOrderIndexAscIdAsc(Long sectionId, ContentStatus status);
    long countBySection_Id(Long sectionId);
    long countBySection_Category_Id(Long categoryId);

    @org.springframework.data.jpa.repository.Query("""
            select l from Lesson l
            where l.id = :lessonId
              and l.isDeleted = false
              and l.status = :status
              and l.section.isDeleted = false
              and l.section.status = :status
              and l.section.category.isDeleted = false
              and l.section.category.status = :status
            """)
    Optional<Lesson> findPublishedById(@org.springframework.data.repository.query.Param("lessonId") Long lessonId,
                                       @org.springframework.data.repository.query.Param("status") ContentStatus status);
}
