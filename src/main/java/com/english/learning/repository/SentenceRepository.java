package com.english.learning.repository;

import com.english.learning.entity.Sentence;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findByLesson_IdOrderByOrderIndex(Long lessonId);
}
