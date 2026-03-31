package com.english.learning.service.impl;

import com.english.learning.repository.SectionRepository;
import com.english.learning.entity.Section;
import com.english.learning.service.SectionService;
import org.springframework.stereotype.Service;

import com.english.learning.repository.LessonRepository;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.exception.ResourceInUseException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final LessonRepository lessonRepository;

    @Override
    public List<Section> getSectionsByCategoryId(Long categoryId) {
        return sectionRepository.findByCategory_Id(categoryId);
    }

    @Override
    public Optional<Section> getSectionById(Long id) {
        return sectionRepository.findById(id);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section không tồn tại"));

        if (lessonRepository.countBySection_Id(id) > 0) {
            throw new ResourceInUseException("Không thể xóa Section. Vui lòng di chuyển hoặc xóa các Bài học bên trong trước.");
        }

        section.setIsDeleted(true);
        sectionRepository.save(section);
    }
}
