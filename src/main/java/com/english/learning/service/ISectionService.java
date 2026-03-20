package com.english.learning.service;

import com.english.learning.model.Section;

import java.util.List;
import java.util.Optional;

public interface ISectionService {
    List<Section> getSectionsByCategoryId(Long categoryId);
    Optional<Section> getSectionById(Long id);
}
