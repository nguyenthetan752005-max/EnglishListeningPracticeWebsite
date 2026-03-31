package com.english.learning.service.impl;

import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.entity.Sentence;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.HintService;
import com.english.learning.service.SentenceService;
import com.english.learning.util.TextNormalizerUtil;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.exception.ResourceInUseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SentenceServiceImpl implements SentenceService {

    private final SentenceRepository sentenceRepository;
    private final HintService hintService;
    private final SpeakingResultRepository speakingResultRepository;
    private final UserProgressRepository userProgressRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        for (Sentence sentence : sentences) {
            String cleanText = TextNormalizerUtil.cleanHtmlAndBrackets(sentence.getContent());
            sentence.setContent(cleanText);
            sentence.setProperNouns(hintService.extractProperNouns(cleanText));
        }
        return sentences;
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceRepository.findById(id);
    }

    @Override
    public Map<Long, List<String>> getProperNounHints(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        return hintService.getHintsMap(sentences);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteSentence(Long id) throws Exception {
        Sentence sentence = sentenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Câu không tồn tại."));

        if (speakingResultRepository.countBySentence_Id(id) > 0 || userProgressRepository.countBySentence_Id(id) > 0) {
            throw new ResourceInUseException("Không thể xoá cứng. Dữ liệu đang chịu ràng buộc khóa ngoại (người dùng đã làm bài hoặc lưu ghi âm).");
        }

        if (sentence.getCloudAudioId() != null && !sentence.getCloudAudioId().isEmpty()) {
            cloudinaryService.deleteFile(sentence.getCloudAudioId());
        }

        sentenceRepository.deleteById(id);
    }
}
