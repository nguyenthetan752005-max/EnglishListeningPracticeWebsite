package com.english.learning.service.impl;

import com.english.learning.dao.ICommentDAO;
import com.english.learning.dao.ICommentVoteDAO;
import com.english.learning.dao.ILessonDAO;
import com.english.learning.dao.IUserDAO;
import com.english.learning.model.Comment;
import com.english.learning.model.CommentVote;
import com.english.learning.model.Lesson;
import com.english.learning.model.User;
import com.english.learning.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements ICommentService {

    @Autowired
    private ICommentDAO commentDAO;

    @Autowired
    private ICommentVoteDAO commentVoteDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private ILessonDAO lessonDAO;

    @Override
    public List<Comment> getCommentsByLessonId(Long lessonId) {
        return commentDAO.findByLessonId(lessonId);
    }

    @Override
    public Comment addComment(Long lessonId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setContent(content);

        Lesson lesson = lessonDAO.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        comment.setLesson(lesson);
        comment.setUser(user);

        if (parentId != null) {
            Comment parent = commentDAO.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Comment gốc không tồn tại!"));
            comment.setParent(parent);
        }

        return commentDAO.save(comment);
    }

    @Override
    public CommentVote voteComment(Long commentId, Long userId, Boolean isLike) {
        Optional<CommentVote> existingVote = commentVoteDAO.findByCommentIdAndUserId(commentId, userId);

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            if (vote.getIsLike().equals(isLike)) {
                // Cùng loại → xóa vote (toggle off)
                commentVoteDAO.delete(vote.getId());
                return null;
            } else {
                // Khác loại → đổi is_like
                vote.setIsLike(isLike);
                return commentVoteDAO.save(vote);
            }
        } else {
            // Chưa vote → tạo mới
            CommentVote vote = new CommentVote();
            Comment comment = commentDAO.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại!"));
            User user = userDAO.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            vote.setComment(comment);
            vote.setUser(user);
            vote.setIsLike(isLike);
            return commentVoteDAO.save(vote);
        }
    }
}
