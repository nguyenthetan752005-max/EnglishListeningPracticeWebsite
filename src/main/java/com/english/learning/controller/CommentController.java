package com.english.learning.controller;

import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;
import com.english.learning.entity.User;
import com.english.learning.enums.CommentType;
import com.english.learning.service.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;

    // === REST API endpoints (JSON) ===

    @GetMapping("/sentence/{sentenceId}/comments")
    @ResponseBody
    public List<Comment> getComments(
            @PathVariable Long sentenceId,
            @RequestParam(required = false, defaultValue = "LISTENING") CommentType source) {
        return commentService.getTopLevelCommentsWithVotes(sentenceId, source);
    }

    @GetMapping("/comment/{commentId}/replies")
    @ResponseBody
    public List<Comment> getReplies(@PathVariable Long commentId) {
        return commentService.getRepliesWithVotes(commentId);
    }

    @PostMapping("/comment")
    @ResponseBody
    public ResponseEntity<?> addComment(
            @RequestParam Long sentenceId,
            @RequestParam String content,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false, defaultValue = "LISTENING") CommentType source,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập để bình luận!"));
        }
        Comment saved = commentService.addComment(sentenceId, user.getId(), content, parentId, source);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/comment/{commentId}/vote")
    @ResponseBody
    public ResponseEntity<?> voteComment(
            @PathVariable Long commentId,
            @RequestParam Boolean isLike,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập để vote!"));
        }
        CommentVote vote = commentService.voteComment(commentId, user.getId(), isLike);
        return ResponseEntity.ok(vote != null ? vote : Map.of("removed", true));
    }

    @DeleteMapping("/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập!"));
        }
        commentService.deleteComment(commentId, user.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/comment/{commentId}")
    @ResponseBody
    public ResponseEntity<?> editComment(
            @PathVariable Long commentId,
            @RequestParam String content,
            HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn cần đăng nhập!"));
        }
        Comment edited = commentService.editComment(commentId, user.getId(), content);
        return ResponseEntity.ok(edited);
    }

    // === MVC (Thymeleaf) endpoints ===

    @GetMapping("/my-comments")
    public String myComments(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        List<Comment> comments = commentService.getCommentsByUserId(user.getId());
        model.addAttribute("comments", comments);
        model.addAttribute("user", user);
        return "user/my-comments";
    }
}
