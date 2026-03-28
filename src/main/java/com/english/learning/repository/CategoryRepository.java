package com.english.learning.repository;

import com.english.learning.entity.Category;
import com.english.learning.enums.PracticeType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByPracticeType(PracticeType practiceType);
}
