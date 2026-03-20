package com.english.learning.service.impl;

import com.english.learning.dao.ILessonDAO;
import com.english.learning.dao.IUserDAO;
import com.english.learning.dao.IUserProgressDAO;
import com.english.learning.model.Lesson;
import com.english.learning.model.User;
import com.english.learning.model.UserProgress;
import com.english.learning.service.IUserProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserProgressServiceImpl implements IUserProgressService {

    @Autowired
    private IUserProgressDAO userProgressDAO;

    @Autowired
    private IUserDAO userDAO;

    @Autowired
    private ILessonDAO lessonDAO;

    @Override
    public Optional<UserProgress> getProgress(Long userId, Long lessonId) {
        return userProgressDAO.findByUserIdAndLessonId(userId, lessonId);
    }

    @Override
    public List<UserProgress> getProgressByUserId(Long userId) {
        return userProgressDAO.findByUserId(userId);
    }

    @Override
    public UserProgress updateProgress(Long userId, Long lessonId, Integer completedSentences) {
        Optional<UserProgress> progressOpt = userProgressDAO.findByUserIdAndLessonId(userId, lessonId);

        UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            progress.setCompletedSentences(completedSentences);
            progress.setStatus("IN_PROGRESS");
        } else {
            progress = new UserProgress();
            User user = userDAO.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Lesson lesson = lessonDAO.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
            progress.setUser(user);
            progress.setLesson(lesson);
            progress.setCompletedSentences(completedSentences);
            progress.setStatus("IN_PROGRESS");
        }

        return userProgressDAO.save(progress);
    }

    @Override
    public UserProgress completeLesson(Long userId, Long lessonId) {
        Optional<UserProgress> progressOpt = userProgressDAO.findByUserIdAndLessonId(userId, lessonId);

        UserProgress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = new UserProgress();
            User user = userDAO.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Lesson lesson = lessonDAO.findById(lessonId)
                    .orElseThrow(() -> new RuntimeException("Lesson không tồn tại!"));
            progress.setUser(user);
            progress.setLesson(lesson);
        }
        progress.setStatus("COMPLETED");
        return userProgressDAO.save(progress);
    }
}
