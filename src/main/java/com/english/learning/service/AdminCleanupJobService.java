package com.english.learning.service;

import com.english.learning.entity.SpeakingResult;
import com.english.learning.repository.PasswordResetTokenRepository;
import com.english.learning.repository.SpeakingResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCleanupJobService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final CloudinaryService cloudinaryService;

    // Chạy vào 2:00 AM mỗi ngày
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTokens() {
        log.info("Bắt đầu dọn dẹp PasswordResetToken hết hạn...");
        try {
            passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
            log.info("Dọn dẹp PasswordResetToken hoàn tất.");
        } catch (Exception e) {
            log.error("Lỗi dọn dẹp PasswordResetToken: " + e.getMessage());
        }
    }

    // Chạy vào ngày 1 hàng tháng lúc 3:00 AM
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldSpeakingResults() {
        log.info("Bắt đầu dọn dẹp SpeakingResult cũ hơn 6 tháng...");
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<SpeakingResult> oldResults = speakingResultRepository.findByUpdatedAtBefore(sixMonthsAgo);

        int count = 0;
        for (SpeakingResult result : oldResults) {
            try {
                if (result.getUserAudioPublicId() != null && !result.getUserAudioPublicId().isEmpty()) {
                    cloudinaryService.deleteFile(result.getUserAudioPublicId());
                } else {
                    log.warn("SpeakingResult ID " + result.getId() + " không có publicId để xoá trên Cloudinary.");
                }
                speakingResultRepository.delete(result);
                count++;
            } catch (Exception e) {
                log.error("Lỗi xóa file Cloudinary cho SpeakingResult ID " + result.getId() + ": " + e.getMessage());
                // Không throw exception để tiếp tục xoá các record khác
            }
        }
        
        log.info("Đã dọn dẹp thành công " + count + " bản ghi SpeakingResult cũ.");
    }
}
