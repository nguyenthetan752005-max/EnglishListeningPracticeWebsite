package com.english.learning.service.impl;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.enums.SpeakingResultType;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.AiScoringService;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.SpeakingService;
import com.english.learning.service.WitAIAudioService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SpeakingServiceImpl implements SpeakingService {

    private final WitAIAudioService witAIAudioService;
    private final AiScoringService aiScoringService;
    private final CloudinaryService cloudinaryService;
    private final SpeakingResultRepository speakingResultRepository;
    private final UserRepository userRepository;
    private final SentenceRepository sentenceRepository;

    public SpeakingServiceImpl(WitAIAudioService witAIAudioService,
                               AiScoringService aiScoringService,
                               CloudinaryService cloudinaryService,
                               SpeakingResultRepository speakingResultRepository,
                               UserRepository userRepository,
                               SentenceRepository sentenceRepository) {
        this.witAIAudioService = witAIAudioService;
        this.aiScoringService = aiScoringService;
        this.cloudinaryService = cloudinaryService;
        this.speakingResultRepository = speakingResultRepository;
        this.userRepository = userRepository;
        this.sentenceRepository = sentenceRepository;
    }

    @Override
    public SpeakingResultDTO evaluateSpeaking(MultipartFile audio, String referenceText, Long userId, Long sentenceId) {
        // 1. Transcribe audio bằng Wit.AI
        String transcribedText = "";
        try {
            transcribedText = witAIAudioService.transcribeAudio(audio);
        } catch (Exception e) {
            System.err.println("Error transcribing audio (silent or noisy?): " + e.getMessage());
            // Instead of throwing an exception (which causes 500 error), just treat it as empty text.
            transcribedText = "";
        }

        // 2. Chấm điểm bằng AI (Groq)
        SpeakingResult aiScore = aiScoringService.scoreSpeaking(referenceText, transcribedText);
        String feedback = aiScore.getFeedback();

        // 3. Build DTO kết quả hiện tại (Current)
        SpeakingResultDTO dto = new SpeakingResultDTO();
        dto.setReferenceText(referenceText);
        dto.setTranscribedText(transcribedText);
        dto.setScore(aiScore.getScore());
        dto.setFeedback(feedback);

        // 4. Nếu đã đăng nhập → upload Cloudinary + lưu DB
        if (userId != null && sentenceId != null) {
            System.out.println("Bắt đầu lưu Speaking Result cho User ID: " + userId + ", Sentence ID: " + sentenceId);
            try {
                // Upload audio lên Cloudinary (ghi đè file cũ cùng public_id)
                String currentPublicId = "user_" + userId + "_sentence_" + sentenceId + "_current";
                Map<String, String> uploadResult = cloudinaryService.uploadAudio(audio.getBytes(), currentPublicId);
                String audioUrl = uploadResult.get("url");
                dto.setAudioUrl(audioUrl);

                // Lấy user + sentence entity
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User không tồn tại!"));
                Sentence sentence = sentenceRepository.findById(sentenceId)
                        .orElseThrow(() -> new RuntimeException("Sentence không tồn tại!"));

                // --- Xử lý CURRENT: Tìm và ghi đè hoặc tạo mới ---
                SpeakingResult currentResult = speakingResultRepository
                        .findByUser_IdAndSentence_IdAndResultType(userId, sentenceId, SpeakingResultType.CURRENT)
                        .orElse(new SpeakingResult());

                currentResult.setUser(user);
                currentResult.setSentence(sentence);
                currentResult.setResultType(SpeakingResultType.CURRENT);
                currentResult.setScore(aiScore.getScore());
                currentResult.setRecognizedText(transcribedText);
                currentResult.setFeedback(feedback);
                currentResult.setUserAudioUrl(audioUrl);
                speakingResultRepository.save(currentResult);

                // --- Xử lý BEST: Cập nhật nếu điểm mới cao hơn ---
                Optional<SpeakingResult> bestOpt = speakingResultRepository
                        .findByUser_IdAndSentence_IdAndResultType(userId, sentenceId, SpeakingResultType.BEST);

                if (bestOpt.isPresent()) {
                    SpeakingResult bestResult = bestOpt.get();
                    if (aiScore.getScore() > bestResult.getScore()) {
                        // Điểm mới cao hơn → upload audio best và cập nhật
                        String bestPublicId = "user_" + userId + "_sentence_" + sentenceId + "_best";
                        Map<String, String> uploadBestResult = cloudinaryService.uploadAudio(audio.getBytes(), bestPublicId);
                        String bestAudioUrl = uploadBestResult.get("url");

                        bestResult.setScore(aiScore.getScore());
                        bestResult.setRecognizedText(transcribedText);
                        bestResult.setFeedback(feedback);
                        bestResult.setUserAudioUrl(bestAudioUrl);
                        speakingResultRepository.save(bestResult);

                        // Trả DTO với best mới
                        dto.setBestResult(new SpeakingResultDTO.BestResult(
                                aiScore.getScore(), transcribedText, feedback, bestAudioUrl));
                    } else {
                        // Giữ best cũ
                        dto.setBestResult(new SpeakingResultDTO.BestResult(
                                bestResult.getScore(), bestResult.getRecognizedText(),
                                bestResult.getFeedback(), bestResult.getUserAudioUrl()));
                    }
                } else {
                    // Chưa có BEST → tạo mới (lần nói đầu tiên)
                    String bestPublicId = "user_" + userId + "_sentence_" + sentenceId + "_best";
                    Map<String, String> uploadBestResult = cloudinaryService.uploadAudio(audio.getBytes(), bestPublicId);
                    String bestAudioUrl = uploadBestResult.get("url");

                    SpeakingResult newBest = new SpeakingResult();
                    newBest.setUser(user);
                    newBest.setSentence(sentence);
                    newBest.setResultType(SpeakingResultType.BEST);
                    newBest.setScore(aiScore.getScore());
                    newBest.setRecognizedText(transcribedText);
                    newBest.setFeedback(feedback);
                    newBest.setUserAudioUrl(bestAudioUrl);
                    speakingResultRepository.save(newBest);

                    dto.setBestResult(new SpeakingResultDTO.BestResult(
                            aiScore.getScore(), transcribedText, feedback, bestAudioUrl));
                }
            } catch (Exception e) {
                System.err.println("============= LỖI UPLOAD/LƯU SPEAKING RESULT =============");
                System.err.println("Lý do lỗi: " + e.getMessage());
                e.printStackTrace();
                // Vẫn trả kết quả chấm điểm dù lưu thất bại
            }
        } else {
             System.out.println("Bỏ qua lưu DB vì userId (" + userId + ") hoặc sentenceId (" + sentenceId + ") bị null!");
        }

        return dto;
    }

    @Override
    public SpeakingResultDTO getSavedResults(Long userId, Long sentenceId) {
        if (userId == null || sentenceId == null) {
            return null;
        }

        List<SpeakingResult> results = speakingResultRepository.findByUser_IdAndSentence_Id(userId, sentenceId);
        if (results.isEmpty()) {
            return null;
        }

        SpeakingResultDTO dto = new SpeakingResultDTO();

        for (SpeakingResult r : results) {
            if (r.getResultType() == SpeakingResultType.CURRENT) {
                dto.setScore(r.getScore());
                dto.setTranscribedText(r.getRecognizedText());
                dto.setFeedback(r.getFeedback());
                dto.setAudioUrl(r.getUserAudioUrl());
            } else if (r.getResultType() == SpeakingResultType.BEST) {
                dto.setBestResult(new SpeakingResultDTO.BestResult(
                        r.getScore(), r.getRecognizedText(),
                        r.getFeedback(), r.getUserAudioUrl()));
            }
        }

        return dto;
    }
}
