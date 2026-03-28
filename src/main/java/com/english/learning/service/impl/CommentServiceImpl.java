package com.english.learning.service.impl;

import com.english.learning.enums.CommentType;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.CommentVoteRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import com.english.learning.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentVoteRepository commentVoteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SentenceRepository sentenceRepository;

    @Override
    public List<Comment> getCommentsBySentenceId(Long sentenceId) {
        return commentRepository.findBySentence_Id(sentenceId);
    }

    @Override
    public List<Comment> getTopLevelCommentsWithVotes(Long sentenceId, CommentType commentType) {
        List<Comment> comments = commentRepository
                .findBySentence_IdAndCommentTypeAndParentIsNullOrderByCreatedAtDesc(sentenceId, commentType);
        comments.forEach(this::populateVoteCounts);
        return comments;
    }

    @Override
    public List<Comment> getRepliesWithVotes(Long parentId) {
        List<Comment> replies = commentRepository.findByParent_IdOrderByCreatedAtAsc(parentId);
        replies.forEach(this::populateVoteCounts);
        return replies;
    }

    @Override
    public List<Comment> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        comments.forEach(this::populateVoteCounts);
        return comments;
    }

    private void populateVoteCounts(Comment comment) {
        comment.setLikeCount(commentVoteRepository.countVotes(comment.getId(), true));
        comment.setDislikeCount(commentVoteRepository.countVotes(comment.getId(), false));
    }

    @Override
    public Comment addComment(Long sentenceId, Long userId, String content, Long parentId, CommentType source) {
        Comment comment = new Comment();
        comment.setContent(content);
        if (source != null) {
            comment.setCommentType(source);
        } else {
            comment.setCommentType(CommentType.LISTENING);
        }

        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại!"));

        comment.setSentence(sentence);
        comment.setUser(user);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Comment gốc không tồn tại!"));
            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);
        populateVoteCounts(saved);
        return saved;
    }

    @Override
    public CommentVote voteComment(Long commentId, Long userId, Boolean isLike) {
        Optional<CommentVote> existingVote = commentVoteRepository.findByComment_IdAndUser_Id(commentId, userId);

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            if (vote.getIsLike().equals(isLike)) {
                // Cùng loại → xóa vote (toggle off)
                commentVoteRepository.deleteById(vote.getId());
                return null;
            } else {
                // Khác loại → đổi is_like
                vote.setIsLike(isLike);
                return commentVoteRepository.save(vote);
            }
        } else {
            // Chưa vote → tạo mới
            CommentVote vote = new CommentVote();
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment không tồn tại!"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            vote.setComment(comment);
            vote.setUser(user);
            vote.setIsLike(isLike);
            return commentVoteRepository.save(vote);
        }
    }

    @Override
    public Comment editComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại!"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa comment này!");
        }
        comment.setContent(newContent + " [author edited comment]");
        Comment saved = commentRepository.save(comment);
        populateVoteCounts(saved);
        return saved;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại!"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xóa comment này!");
        }
        // Nếu có reply → soft delete (giữ comment, đổi nội dung)
        List<Comment> replies = commentRepository.findByParent_IdOrderByCreatedAtAsc(commentId);
        if (replies != null && !replies.isEmpty()) {
            comment.setContent("comment has been deleted by author");
            commentRepository.save(comment);
        } else {
            commentRepository.deleteById(commentId);
        }
    }
}
