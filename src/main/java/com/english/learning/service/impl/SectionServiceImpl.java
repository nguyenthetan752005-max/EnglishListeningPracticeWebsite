package com.english.learning.service.impl;

import com.english.learning.dao.ISectionDAO;
import com.english.learning.model.Section;
import com.english.learning.service.ISectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectionServiceImpl implements ISectionService {

    @Autowired
    private ISectionDAO sectionDAO;

    @Override
    public List<Section> getSectionsByCategoryId(Long categoryId) {
        return sectionDAO.findByCategoryId(categoryId);
    }

    @Override
    public Optional<Section> getSectionById(Long id) {
        return sectionDAO.findById(id);
    }
}
