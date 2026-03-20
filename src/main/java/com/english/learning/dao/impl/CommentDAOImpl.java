package com.english.learning.dao.impl;

import com.english.learning.dao.ICommentDAO;
import com.english.learning.model.Comment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CommentDAOImpl implements ICommentDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Comment> findByLessonId(Long lessonId) {
        return entityManager
                .createQuery("SELECT c FROM Comment c WHERE c.lesson.id = :lessonId ORDER BY c.createdAt DESC", Comment.class)
                .setParameter("lessonId", lessonId)
                .getResultList();
    }

    @Override
    public Optional<Comment> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Comment.class, id));
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            entityManager.persist(comment);
            return comment;
        } else {
            return entityManager.merge(comment);
        }
    }

    @Override
    public void delete(Long id) {
        Comment comment = entityManager.find(Comment.class, id);
        if (comment != null) {
            entityManager.remove(comment);
        }
    }
}
