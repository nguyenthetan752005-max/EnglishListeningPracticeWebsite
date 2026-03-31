package com.english.learning.service.impl;

import com.english.learning.repository.LessonRepository;
import com.english.learning.entity.Lesson;
import com.english.learning.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    @Override
    public List<Lesson> getLessonsBySectionId(Long sectionId) {
        return lessonRepository.findBySection_Id(sectionId);
    }

    @Override
    public Optional<Lesson> getLessonById(Long id) {
        return lessonRepository.findById(id);
    }

    @Override
    public com.english.learning.dto.LessonNavigationDTO getLessonNavigation(Lesson currentLesson, com.english.learning.enums.PracticeType practiceType) {
        com.english.learning.entity.Section section = currentLesson.getSection();
        Lesson nextLesson = null;
        boolean isLastLessonInSection = false;

        if (section != null) {
            List<Lesson> sectionLessons = getLessonsBySectionId(section.getId()).stream()
                    .filter(l -> l.getSection().getCategory().getPracticeType() == practiceType)
                    .sorted(java.util.Comparator.comparing(Lesson::getId))
                    .toList();
            
            int currentIndex = -1;
            for (int i = 0; i < sectionLessons.size(); i++) {
                if (sectionLessons.get(i).getId().equals(currentLesson.getId())) {
                    currentIndex = i;
                    break;
                }
            }
            
            if (currentIndex >= 0 && currentIndex < sectionLessons.size() - 1) {
                nextLesson = sectionLessons.get(currentIndex + 1);
            } else if (currentIndex == sectionLessons.size() - 1) {
                isLastLessonInSection = true;
            }
        }
        com.english.learning.dto.LessonDTO nextLessonDto = null;
        if (nextLesson != null) {
            nextLessonDto = com.english.learning.dto.LessonDTO.builder()
                .id(nextLesson.getId())
                .sectionId(nextLesson.getSection() != null ? nextLesson.getSection().getId() : null)
                .type(nextLesson.getType())
                .youtubeVideoId(nextLesson.getYoutubeVideoId())
                .title(nextLesson.getTitle())
                .level(nextLesson.getLevel())
                .totalSentences(nextLesson.getTotalSentences())
                .build();
        }
        return new com.english.learning.dto.LessonNavigationDTO(nextLessonDto, isLastLessonInSection);
    }
}
