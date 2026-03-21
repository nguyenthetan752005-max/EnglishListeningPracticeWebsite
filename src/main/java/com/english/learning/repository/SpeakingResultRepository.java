package com.english.learning.repository;

import com.english.learning.entity.SpeakingResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SpeakingResultRepository extends JpaRepository<SpeakingResult, Long> {
    Optional<SpeakingResult> findByUser_IdAndSentence_Id(Long userId, Long sentenceId);
    List<SpeakingResult> findByUser_Id(Long userId);
}
