package com.english.learning.dao.impl;

import com.english.learning.dao.IUserProgressDAO;
import com.english.learning.model.UserProgress;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class UserProgressDAOImpl implements IUserProgressDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<UserProgress> findByUserIdAndLessonId(Long userId, Long lessonId) {
        List<UserProgress> results = entityManager
                .createQuery("SELECT up FROM UserProgress up WHERE up.user.id = :userId AND up.lesson.id = :lessonId", UserProgress.class)
                .setParameter("userId", userId)
                .setParameter("lessonId", lessonId)
                .getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<UserProgress> findByUserId(Long userId) {
        return entityManager
                .createQuery("SELECT up FROM UserProgress up WHERE up.user.id = :userId", UserProgress.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public UserProgress save(UserProgress userProgress) {
        if (userProgress.getId() == null) {
            entityManager.persist(userProgress);
            return userProgress;
        } else {
            return entityManager.merge(userProgress);
        }
    }
}
