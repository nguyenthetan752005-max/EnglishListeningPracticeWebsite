package com.english.learning.dao.impl;

import com.english.learning.dao.ISpeakingResultDAO;
import com.english.learning.model.SpeakingResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class SpeakingResultDAOImpl implements ISpeakingResultDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<SpeakingResult> findByUserIdAndSentenceId(Long userId, Long sentenceId) {
        List<SpeakingResult> results = entityManager
                .createQuery("SELECT sr FROM SpeakingResult sr WHERE sr.user.id = :userId AND sr.sentence.id = :sentenceId", SpeakingResult.class)
                .setParameter("userId", userId)
                .setParameter("sentenceId", sentenceId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<SpeakingResult> findByUserId(Long userId) {
        return entityManager
                .createQuery("SELECT sr FROM SpeakingResult sr WHERE sr.user.id = :userId ORDER BY sr.createdAt DESC", SpeakingResult.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public SpeakingResult save(SpeakingResult speakingResult) {
        if (speakingResult.getId() == null) {
            entityManager.persist(speakingResult);
            return speakingResult;
        } else {
            return entityManager.merge(speakingResult);
        }
    }
}
