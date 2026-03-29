package com.english.learning.service.impl;

import com.english.learning.dto.InProgressLessonDTO;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.SectionRepository;
import com.english.learning.repository.CategoryRepository;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.Category;
import com.english.learning.entity.User;
import com.english.learning.entity.UserProgress;
import com.english.learning.enums.UserProgressStatus;
import com.english.learning.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProgressServiceImpl implements UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SentenceRepository sentenceRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public Optional<UserProgress> getProgress(Long userId, Long sentenceId) {
        return userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);
    }

    @Override
    public List<UserProgress> getProgressByUserIdAndLessonId(Long userId, Long lessonId) {
        return userProgressRepository.findByUserIdAndLessonId(userId, lessonId);
    }

    @Override
    public Map<Long, String> getUserProgressMapAsStrings(Long userId, Long lessonId) {
        List<UserProgress> progressList = getProgressByUserIdAndLessonId(userId, lessonId);
        return progressList.stream()
                .collect(Collectors.toMap(
                    up -> up.getSentence().getId(),
                    up -> up.getStatus().name()
                ));
    }

    @Override
    public List<UserProgress> getProgressByUserId(Long userId) {
        return userProgressRepository.findByUser_Id(userId);
    }
    
    // Status calculations
    @Override
    public UserProgressStatus getLessonStatus(Long userId, Long lessonId) {
        long totalSentences = sentenceRepository.countByLesson_Id(lessonId);
        if (totalSentences == 0) return null;

        long completedSentences = userProgressRepository.countByUserIdAndLessonIdAndStatus(userId, lessonId, UserProgressStatus.COMPLETED);
        
        if (completedSentences == totalSentences) {
            return UserProgressStatus.COMPLETED;
        }

        long anyProgressCount = userProgressRepository.countByUserIdAndLessonId(userId, lessonId);
        if (anyProgressCount > 0) {
            return UserProgressStatus.IN_PROGRESS;
        }
        
        return null;
    }

    @Override
    public UserProgressStatus getSectionStatus(Long userId, Long sectionId) {
        long totalSentences = sentenceRepository.countByLesson_Section_Id(sectionId);
        if (totalSentences == 0) return null;

        long completedSentences = userProgressRepository.countByUserIdAndSectionIdAndStatus(userId, sectionId, UserProgressStatus.COMPLETED);
        
        if (completedSentences == totalSentences) {
            return UserProgressStatus.COMPLETED;
        }

        long anyProgressCount = userProgressRepository.countByUserIdAndSectionId(userId, sectionId);
        if (anyProgressCount > 0) {
            return UserProgressStatus.IN_PROGRESS;
        }
        
        return null;
    }

    @Override
    public UserProgressStatus getCategoryStatus(Long userId, Long categoryId) {
        long totalSentences = sentenceRepository.countByLesson_Section_Category_Id(categoryId);
        if (totalSentences == 0) return null;

        long completedSentences = userProgressRepository.countByUserIdAndCategoryIdAndStatus(userId, categoryId, UserProgressStatus.COMPLETED);
        
        if (completedSentences == totalSentences) {
            return UserProgressStatus.COMPLETED;
        }

        long anyProgressCount = userProgressRepository.countByUserIdAndCategoryId(userId, categoryId);
        if (anyProgressCount > 0) {
            return UserProgressStatus.IN_PROGRESS;
        }
        
        return null;
    }

    @Override
    public UserProgress updateProgress(Long userId, Long sentenceId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);

        if (progressOpt.isPresent()) {
            UserProgress progress = progressOpt.get();
            // Nếu đã COMPLETED thì không cho phép thay đổi status sang IN_PROGRESS
            if (progress.getStatus() == UserProgressStatus.COMPLETED) {
                return progress;
            }
            progress.setStatus(UserProgressStatus.IN_PROGRESS);
            return userProgressRepository.save(progress);
        } else {
            UserProgress progress = new UserProgress();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Sentence sentence = sentenceRepository.findById(sentenceId)
                    .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));
            progress.setUser(user);
            progress.setSentence(sentence);
            progress.setStatus(UserProgressStatus.IN_PROGRESS);
            return userProgressRepository.save(progress);
        }
    }

    @Override
    public UserProgress completeSentence(Long userId, Long sentenceId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);

        UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = new UserProgress();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Sentence sentence = sentenceRepository.findById(sentenceId)
                    .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));
            progress.setUser(user);
            progress.setSentence(sentence);
        }
        progress.setStatus(UserProgressStatus.COMPLETED);
        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress skipSentence(Long userId, Long sentenceId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndSentence_Id(userId, sentenceId);

        UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            // Nếu đã COMPLETED thì không cho phép thay đổi sang SKIPPED
            if (progress.getStatus() == UserProgressStatus.COMPLETED) {
                return progress;
            }
        } else {
            progress = new UserProgress();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Sentence sentence = sentenceRepository.findById(sentenceId)
                    .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));
            progress.setUser(user);
            progress.setSentence(sentence);
        }
        progress.setStatus(UserProgressStatus.SKIPPED);
        return userProgressRepository.save(progress);
    }

    @Override
    public List<InProgressLessonDTO> getInProgressLessons(Long userId) {
        List<InProgressLessonDTO> result = new ArrayList<>();
        
        // Get all lessons that have any progress (IN_PROGRESS or SKIPPED status)
        List<Long> lessonIdsWithProgress = userProgressRepository.findLessonIdsWithProgressByUserId(userId);
        
        for (Long lessonId : lessonIdsWithProgress) {
            long totalSentences = sentenceRepository.countByLesson_Id(lessonId);
            if (totalSentences == 0) continue;
            
            long completedCount = userProgressRepository.countByUserIdAndLessonIdAndStatus(userId, lessonId, UserProgressStatus.COMPLETED);
            if (completedCount == totalSentences) {
                continue; // Skip fully completed lessons
            }
            
            Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
            if (lessonOpt.isEmpty()) continue;
            
            Lesson lesson = lessonOpt.get();
            Section section = lesson.getSection();
            Category category = section != null ? section.getCategory() : null;
            
            List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
            List<UserProgress> progressList = userProgressRepository.findByUserIdAndLessonId(userId, lessonId);
            java.util.Map<Long, UserProgressStatus> progressMap = progressList.stream()
                .collect(Collectors.toMap(up -> up.getSentence().getId(), UserProgress::getStatus));
            
            Long firstUncompletedSentenceId = null;
            int firstUncompletedIndex = 0;
            for (int i = 0; i < sentences.size(); i++) {
                Sentence s = sentences.get(i);
                UserProgressStatus status = progressMap.get(s.getId());
                if (status != UserProgressStatus.COMPLETED) {
                    firstUncompletedSentenceId = s.getId();
                    firstUncompletedIndex = i;
                    break;
                }
            }
            
            InProgressLessonDTO dto = new InProgressLessonDTO(
                lessonId,
                lesson.getTitle(),
                category != null ? category.getName() : "",
                section != null ? section.getName() : "",
                lesson.getSection().getCategory().getPracticeType() != null ? lesson.getSection().getCategory().getPracticeType().name() : "LISTENING",
                (int) totalSentences,
                (int) completedCount,
                firstUncompletedSentenceId,
                firstUncompletedIndex
            );
            result.add(dto);
        }
        
        return result;
    }
}
