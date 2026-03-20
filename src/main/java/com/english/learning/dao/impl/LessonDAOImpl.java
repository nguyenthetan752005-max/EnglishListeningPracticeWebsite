package com.english.learning.dao.impl;

import com.english.learning.dao.ILessonDAO;
import com.english.learning.model.Lesson;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class LessonDAOImpl implements ILessonDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Lesson> findBySectionId(Long sectionId) {
        return entityManager
                .createQuery("SELECT l FROM Lesson l WHERE l.section.id = :sectionId", Lesson.class)
                .setParameter("sectionId", sectionId)
                .getResultList();
    }

    @Override
    public Optional<Lesson> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Lesson.class, id));
    }

    @Override
    public Lesson save(Lesson lesson) {
        if (lesson.getId() == null) {
            entityManager.persist(lesson);
            return lesson;
        } else {
            return entityManager.merge(lesson);
        }
    }

    @Override
    public void delete(Long id) {
        Lesson lesson = entityManager.find(Lesson.class, id);
        if (lesson != null) {
            entityManager.remove(lesson);
        }
    }
}
