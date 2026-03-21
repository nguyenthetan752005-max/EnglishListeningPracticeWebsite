package com.english.learning.repository;

import com.english.learning.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByComment_IdAndUser_Id(Long commentId, Long userId);
}
