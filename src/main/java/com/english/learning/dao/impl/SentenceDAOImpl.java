package com.english.learning.dao.impl;

import com.english.learning.dao.ISentenceDAO;
import com.english.learning.model.Sentence;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class SentenceDAOImpl implements ISentenceDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Sentence> findByLessonIdOrderByOrderIndex(Long lessonId) {
        return entityManager
                .createQuery("SELECT s FROM Sentence s WHERE s.lesson.id = :lessonId ORDER BY s.orderIndex", Sentence.class)
                .setParameter("lessonId", lessonId)
                .getResultList();
    }

    @Override
    public Optional<Sentence> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Sentence.class, id));
    }

    @Override
    public Sentence save(Sentence sentence) {
        if (sentence.getId() == null) {
            entityManager.persist(sentence);
            return sentence;
        } else {
            return entityManager.merge(sentence);
        }
    }

    @Override
    public void delete(Long id) {
        Sentence sentence = entityManager.find(Sentence.class, id);
        if (sentence != null) {
            entityManager.remove(sentence);
        }
    }
}
