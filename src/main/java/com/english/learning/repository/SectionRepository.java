package com.english.learning.repository;

import com.english.learning.entity.Section;
import com.english.learning.enums.ContentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCategory_Id(Long categoryId);
    List<Section> findByCategoryId(Long categoryId);
    List<Section> findByCategory_IdOrderByOrderIndexAscIdAsc(Long categoryId);
    List<Section> findByCategory_IdAndStatusOrderByOrderIndexAscIdAsc(Long categoryId, ContentStatus status);
    long countByCategory_Id(Long categoryId);

    @org.springframework.data.jpa.repository.Query("""
            select s from Section s
            where s.id = :sectionId
              and s.category.id = :categoryId
              and s.isDeleted = false
              and s.status = :status
              and s.category.isDeleted = false
              and s.category.status = :status
            """)
    Optional<Section> findPublishedByIdAndCategoryId(@org.springframework.data.repository.query.Param("sectionId") Long sectionId,
                                                     @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
                                                     @org.springframework.data.repository.query.Param("status") ContentStatus status);
}
