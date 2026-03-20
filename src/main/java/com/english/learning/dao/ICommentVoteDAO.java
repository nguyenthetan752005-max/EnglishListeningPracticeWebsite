package com.english.learning.dao;

import com.english.learning.model.CommentVote;

import java.util.Optional;

public interface ICommentVoteDAO {
    Optional<CommentVote> findByCommentIdAndUserId(Long commentId, Long userId);
    CommentVote save(CommentVote commentVote);
    void delete(Long id);
}
