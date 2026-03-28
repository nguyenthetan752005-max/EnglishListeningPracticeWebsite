package com.english.learning.service.impl;

import com.english.learning.repository.LessonRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.entity.Lesson;
import com.english.learning.entity.User;
import com.english.learning.entity.UserProgress;
import com.english.learning.service.UserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProgressServiceImpl implements UserProgressService {

    @Autowired
    private UserProgressRepository userProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Override
    public Optional<UserProgress> getProgress(Long userId, Long lessonId) {
        return userProgressRepository.findByUser_IdAndLesson_Id(userId, lessonId);
    }

    @Override
    public List<UserProgress> getProgressByUserId(Long userId) {
        return userProgressRepository.findByUser_Id(userId);
    }

    @Override
    public UserProgress updateProgress(Long userId, Long lessonId, Integer completedSentences) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndLesson_Id(userId, lessonId);

        UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            progress.setCompletedSentences(completedSentences);
            progress.setStatus("IN_PROGRESS");
        } else {
            progress = new UserProgress();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
            progress.setUser(user);
            progress.setLesson(lesson);
            progress.setCompletedSentences(completedSentences);
            progress.setStatus("IN_PROGRESS");
        }

        return userProgressRepository.save(progress);
    }

    @Override
    public UserProgress completeLesson(Long userId, Long lessonId) {
        Optional<UserProgress> progressOpt = userProgressRepository.findByUser_IdAndLesson_Id(userId, lessonId);

        UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = new UserProgress();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Lesson lesson = lessonRepository.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
            progress.setUser(user);
            progress.setLesson(lesson);
        }
        progress.setStatus("COMPLETED");
        return userProgressRepository.save(progress);
    }
}
