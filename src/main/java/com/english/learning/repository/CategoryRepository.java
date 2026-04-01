package com.english.learning.repository;

import com.english.learning.entity.Category;
import com.english.learning.enums.ContentStatus;
import com.english.learning.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByPracticeType(PracticeType practiceType);
    List<Category> findAllByOrderByOrderIndexAscIdAsc();
    List<Category> findByStatusOrderByOrderIndexAscIdAsc(ContentStatus status);
    List<Category> findByPracticeTypeAndStatusOrderByOrderIndexAscIdAsc(PracticeType practiceType, ContentStatus status);
    Optional<Category> findByIdAndStatus(Long id, ContentStatus status);
}
