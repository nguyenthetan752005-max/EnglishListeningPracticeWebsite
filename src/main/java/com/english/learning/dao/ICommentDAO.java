package com.english.learning.dao;

import com.english.learning.model.Comment;

import java.util.List;
import java.util.Optional;

public interface ICommentDAO {
    List<Comment> findByLessonId(Long lessonId);
    Optional<Comment> findById(Long id);
    Comment save(Comment comment);
    void delete(Long id);
}
