package com.english.learning.repository;

import com.english.learning.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findBySentence_Id(Long sentenceId);
    List<Comment> findBySentence_IdAndParentIsNullOrderByCreatedAtDesc(Long sentenceId);
    List<Comment> findByParent_IdOrderByCreatedAtAsc(Long parentId);
    List<Comment> findByUser_IdOrderByCreatedAtDesc(Long userId);
}
