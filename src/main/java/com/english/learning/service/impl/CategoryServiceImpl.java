package com.english.learning.service.impl;

import com.english.learning.dao.ICategoryDAO;
import com.english.learning.model.Category;
import com.english.learning.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements ICategoryService {

    @Autowired
    private ICategoryDAO categoryDAO;

    @Override
    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryDAO.findById(id);
    }
}
