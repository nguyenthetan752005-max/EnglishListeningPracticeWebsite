package com.english.learning.repository;

import com.english.learning.entity.CommentVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CommentVoteRepository extends JpaRepository<CommentVote, Long> {
    Optional<CommentVote> findByComment_IdAndUser_Id(Long commentId, Long userId);

    @Query("SELECT COUNT(v) FROM CommentVote v WHERE v.comment.id = :commentId AND v.isLike = :isLike")
    long countVotes(@Param("commentId") Long commentId, @Param("isLike") Boolean isLike);
}
