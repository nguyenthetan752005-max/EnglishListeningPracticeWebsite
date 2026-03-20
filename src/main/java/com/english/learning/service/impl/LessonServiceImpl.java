package com.english.learning.service.impl;

import com.english.learning.dao.ILessonDAO;
import com.english.learning.model.Lesson;
import com.english.learning.service.ILessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LessonServiceImpl implements ILessonService {

    @Autowired
    private ILessonDAO lessonDAO;

    @Override
    public List<Lesson> getLessonsBySectionId(Long sectionId) {
        return lessonDAO.findBySectionId(sectionId);
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        return lessonDAO.findById(id);
    }
}
