package com.english.learning.service;

import com.english.learning.entity.Section;

import java.util.List;
import java.util.Optional;

public interface SectionService {
    List<Section> getSectionsByCategoryId(Long categoryId);
    Optional<Section> getSectionById(Long id);
    void deleteSection(Long id);
}
