package com.english.learning.dao.impl;

import com.english.learning.dao.ISectionDAO;
import com.english.learning.model.Section;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class SectionDAOImpl implements ISectionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Section> findByCategoryId(Long categoryId) {
        return entityManager
                .createQuery("SELECT s FROM Section s WHERE s.category.id = :categoryId", Section.class)
                .setParameter("categoryId", categoryId)
                .getResultList();
    }

    @Override
    public Optional<Section> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Section.class, id));
    }

    @Override
    public Section save(Section section) {
        if (section.getId() == null) {
            entityManager.persist(section);
            return section;
        } else {
            return entityManager.merge(section);
        }
    }

    @Override
    public void delete(Long id) {
        Section section = entityManager.find(Section.class, id);
        if (section != null) {
            entityManager.remove(section);
        }
    }
}
