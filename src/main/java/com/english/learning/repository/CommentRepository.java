package com.english.learning.repository;

import com.english.learning.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByLesson_Id(Long lessonId);
}
