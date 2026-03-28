package com.english.learning.service.impl;

import com.english.learning.repository.CategoryRepository;
import com.english.learning.entity.Category;
import com.english.learning.service.CategoryService;
import com.english.learning.enums.PracticeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getCategoriesByPracticeType(PracticeType practiceType) {
        return categoryRepository.findByPracticeType(practiceType);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public List<String> getExpandedLevels(String levelRange) {
        List<String> ALL_LEVELS = List.of("A1", "A2", "B1", "B2", "C1", "C2");
        List<String> expandedLevels = new java.util.ArrayList<>();
        
        if (levelRange != null && levelRange.contains("-")) {
            String[] parts = levelRange.split("-");
            String start = parts[0].trim().toUpperCase();
            String end = parts[1].trim().toUpperCase();
            int startIdx = ALL_LEVELS.indexOf(start);
            int endIdx = ALL_LEVELS.indexOf(end);
            if (startIdx >= 0 && endIdx >= 0 && startIdx <= endIdx) {
                expandedLevels = ALL_LEVELS.subList(startIdx, endIdx + 1);
            }
        } else if (levelRange != null && !levelRange.trim().isEmpty()) {
            // Single level like "OET" or "A1"
            expandedLevels = List.of(levelRange.trim().toUpperCase());
        }
        
        return expandedLevels;
    }
}
