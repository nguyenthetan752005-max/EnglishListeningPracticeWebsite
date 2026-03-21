package com.english.learning.service.impl;

import com.english.learning.repository.SectionRepository;
import com.english.learning.entity.Section;
import com.english.learning.service.SectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SectionServiceImpl implements SectionService {

    @Autowired
    private SectionRepository sectionRepository;

    @Override
    public List<Section> getSectionsByCategoryId(Long categoryId) {
        return sectionRepository.findByCategory_Id(categoryId);
    }

    @Override
    public Optional<Section> getSectionById(Long id) {
        return sectionRepository.findById(id);
    }
}
