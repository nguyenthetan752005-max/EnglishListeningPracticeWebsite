package com.english.learning.controller;

import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;
import com.english.learning.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/lesson/{lessonId}/comments")
    @ResponseBody
    public List<Comment> getComments(@PathVariable Long lessonId) {
        return commentService.getCommentsByLessonId(lessonId);
    }

    @PostMapping("/comment")
    @ResponseBody
    public Comment addComment(
            @RequestParam Long lessonId,
            @RequestParam Long userId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId) {
        return commentService.addComment(lessonId, userId, content, parentId);
    }

    @PostMapping("/comment/{commentId}/vote")
    @ResponseBody
    public CommentVote voteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @RequestParam Boolean isLike) {
        return commentService.voteComment(commentId, userId, isLike);
    }
}
