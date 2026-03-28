package com.english.learning.service;

import com.english.learning.entity.Category;

import com.english.learning.enums.PracticeType;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();
    List<Category> getCategoriesByPracticeType(PracticeType practiceType);
    Optional<Category> getCategoryById(Long id);
    List<String> getExpandedLevels(String levelRange);
}
