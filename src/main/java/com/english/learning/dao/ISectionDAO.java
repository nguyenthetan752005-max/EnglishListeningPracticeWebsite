package com.english.learning.dao;

import com.english.learning.model.Section;

import java.util.List;
import java.util.Optional;

public interface ISectionDAO {
    List<Section> findByCategoryId(Long categoryId);
    Optional<Section> findById(Long id);
    Section save(Section section);
    void delete(Long id);
}
