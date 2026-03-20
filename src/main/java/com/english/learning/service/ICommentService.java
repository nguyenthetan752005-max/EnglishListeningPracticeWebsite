package com.english.learning.service;

import com.english.learning.model.Comment;
import com.english.learning.model.CommentVote;

import java.util.List;

public interface ICommentService {
    List<Comment> getCommentsByLessonId(Long lessonId);
    Comment addComment(Long lessonId, Long userId, String content, Long parentId);
    CommentVote voteComment(Long commentId, Long userId, Boolean isLike);
}
