package com.english.learning.service.impl;

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
    public Comment addComment(Long sentenceId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setContent(content);

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

        return commentRepository.save(comment);
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
}
