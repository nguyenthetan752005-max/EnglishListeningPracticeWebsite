package com.english.learning.dao;

import com.english.learning.model.Category;

import java.util.List;
import java.util.Optional;

public interface ICategoryDAO {
    List<Category> findAll();
    Optional<Category> findById(Long id);
    Category save(Category category);
    void delete(Long id);
}
