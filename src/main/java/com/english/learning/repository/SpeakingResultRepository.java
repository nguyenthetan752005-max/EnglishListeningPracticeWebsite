package com.english.learning.repository;

import com.english.learning.entity.SpeakingResult;
import com.english.learning.enums.SpeakingResultType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpeakingResultRepository extends JpaRepository<SpeakingResult, Long> {

    // Tìm 1 record theo user + sentence + type (BEST hoặc CURRENT)
    Optional<SpeakingResult> findByUser_IdAndSentence_IdAndResultType(Long userId, Long sentenceId, SpeakingResultType resultType);

    // Lấy cả BEST + CURRENT cho 1 câu
    List<SpeakingResult> findByUser_IdAndSentence_Id(Long userId, Long sentenceId);
}
