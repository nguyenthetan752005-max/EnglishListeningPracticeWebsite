package com.english.learning.repository;

import com.english.learning.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByCategory_Id(Long categoryId);
    List<Section> findByCategoryId(Long categoryId);
}
