package com.english.learning.dao.impl;

import com.english.learning.dao.ICommentVoteDAO;
import com.english.learning.model.CommentVote;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class CommentVoteDAOImpl implements ICommentVoteDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<CommentVote> findByCommentIdAndUserId(Long commentId, Long userId) {
        List<CommentVote> results = entityManager
                .createQuery("SELECT cv FROM CommentVote cv WHERE cv.comment.id = :commentId AND cv.user.id = :userId", CommentVote.class)
                .setParameter("commentId", commentId)
                .setParameter("userId", userId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public CommentVote save(CommentVote commentVote) {
        if (commentVote.getId() == null) {
            entityManager.persist(commentVote);
            return commentVote;
        } else {
            return entityManager.merge(commentVote);
        }
    }

    @Override
    public void delete(Long id) {
        CommentVote vote = entityManager.find(CommentVote.class, id);
        if (vote != null) {
            entityManager.remove(vote);
        }
    }
}
