package com.english.learning.service;

import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;

import java.util.List;

public interface CommentService {
    List<Comment> getCommentsBySentenceId(Long sentenceId);
    Comment addComment(Long sentenceId, Long userId, String content, Long parentId);
    CommentVote voteComment(Long commentId, Long userId, Boolean isLike);
}
