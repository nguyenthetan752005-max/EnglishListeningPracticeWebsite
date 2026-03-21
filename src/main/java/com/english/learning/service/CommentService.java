package com.english.learning.service;

import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;

import java.util.List;

public interface CommentService {
    List<Comment> getCommentsByLessonId(Long lessonId);
    Comment addComment(Long lessonId, Long userId, String content, Long parentId);
    CommentVote voteComment(Long commentId, Long userId, Boolean isLike);
}
