package com.english.learning.repository;

import com.english.learning.entity.DailyStudyStatistic;
import com.english.learning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyStudyStatisticRepository extends JpaRepository<DailyStudyStatistic, Long> {
    
    // Finds the record for today so the tracking service can accumulate seconds
    Optional<DailyStudyStatistic> findByUserAndStudyDate(User user, LocalDate studyDate);

    // Sums up the active time for rolling windows to be cached into User
    @Query("SELECT COALESCE(SUM(d.activeTimeSeconds), 0) FROM DailyStudyStatistic d WHERE d.user = :user AND d.studyDate >= :startDate")
    Integer sumActiveTimeByUserAndStudyDateAfter(@Param("user") User user, @Param("startDate") LocalDate startDate);

    @Modifying
    @Query(value = "DELETE FROM daily_study_statistics WHERE user_id = :userId", nativeQuery = true)
    void deleteByUserId(@Param("userId") Long userId);
}
