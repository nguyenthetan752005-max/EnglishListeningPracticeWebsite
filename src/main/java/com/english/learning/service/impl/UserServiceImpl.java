package com.english.learning.service.impl;

import com.english.learning.enums.Role;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.CommentVoteRepository;
import com.english.learning.repository.DailyStudyStatisticRepository;
import com.english.learning.repository.PasswordResetTokenRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.repository.UserProgressRepository;
import com.english.learning.entity.User;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.service.CloudinaryService;
import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Pattern DELETED_SUFFIX_PATTERN = Pattern.compile("_deleted_\\d+$");

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final DailyStudyStatisticRepository dailyStudyStatisticRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public User register(User user) {
        // Kiểm tra email đã tồn tại
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại!");
        }
        // Kiểm tra username đã tồn tại
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        // Hash password trước khi lưu
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(username);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            String storedPassword = user.getPassword();

            // Hỗ trợ cả tài khoản cũ chưa mã hóa mật khẩu
            if (storedPassword.startsWith("$2a$")) {
                if (BCrypt.checkpw(password, storedPassword)) {
                    return Optional.of(user);
                }
            } else {
                if (storedPassword.equals(password)) {
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateUsername(Long id, String newUsername) throws Exception {
        Optional<User> existingUserOpt = userRepository.findByUsername(newUsername);
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getId().equals(id)) {
            throw new Exception("Tên người dùng đã tồn tại!");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy người dùng"));
        user.setUsername(newUsername);
        userRepository.save(user);
    }

    @Override
    public void updateAvatarUrl(Long id, String avatarUrl) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy người dùng"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    public void updateAvatar(Long id, String avatarUrl, String avatarPublicId) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy người dùng"));
        user.setAvatarUrl(avatarUrl);
        user.setAvatarPublicId(avatarPublicId);
        userRepository.save(user);
    }

    public Optional<User> authenticateAdmin(String username, String password) {
        Optional<User> userOpt = authenticate(username, password);
        if (userOpt.isPresent() && Role.ADMIN.equals(userOpt.get().getRole())) {
            return userOpt;
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = authenticate(username, password);
        if (userOpt.isPresent() && Role.USER.equals(userOpt.get().getRole())) {
            return userOpt;
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }

    @Override
    public void adminUpdateBasicInfo(Long id, String username, String avatarUrl, boolean isActive, Role role) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy người dùng"));
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new Exception("Tên người dùng không được để trống.");
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(normalizedUsername);
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getId().equals(id)) {
            throw new Exception("Tên người dùng đã tồn tại!");
        }

        user.setUsername(normalizedUsername);
        user.setAvatarUrl(normalizeBlank(avatarUrl));
        user.setIsActive(isActive);
        user.setRole(role != null ? role : Role.USER);
        userRepository.save(user);
    }

    @Override
    public void adminUpdatePassword(Long id, String newPassword) throws Exception {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Không tìm thấy người dùng"));
        updatePassword(user, newPassword.trim());
    }

    @Override
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.english.learning.exception.ResourceNotFoundException("Người dùng không tồn tại"));

        String modifier = "_deleted_" + System.currentTimeMillis();
        user.setEmail(user.getEmail() + modifier);
        user.setUsername(user.getUsername() + modifier);
        
        user.setIsDeleted(true);
        user.setIsActive(false);

        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteUser(Long id) throws Exception {
        User user = userRepository.findAnyUserById(id)
                .orElseThrow(() -> new com.english.learning.exception.ResourceNotFoundException("Người dùng không tồn tại"));

        for (SpeakingResult speakingResult : speakingResultRepository.findByUser_Id(id)) {
            if (speakingResult.getUserAudioPublicId() != null && !speakingResult.getUserAudioPublicId().isBlank()) {
                cloudinaryService.deleteFile(speakingResult.getUserAudioPublicId());
            }
        }
        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isBlank()) {
            cloudinaryService.deleteFile(user.getAvatarPublicId());
        }

        passwordResetTokenRepository.deleteByUserId(id);
        commentVoteRepository.deleteByUserId(id);
        commentVoteRepository.deleteByOwnedCommentUserId(id);
        commentRepository.deleteRepliesToOwnedComments(id);
        commentRepository.deleteByUserId(id);
        userProgressRepository.deleteByUserId(id);
        speakingResultRepository.deleteByUserId(id);
        dailyStudyStatisticRepository.deleteByUserId(id);
        userRepository.delete(user);
    }

    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findAnyUserById(id)
                .orElseThrow(() -> new com.english.learning.exception.ResourceNotFoundException("Người dùng không tồn tại"));

        String restoredUsername = stripDeletedSuffix(user.getUsername());
        String restoredEmail = stripDeletedSuffix(user.getEmail());

        userRepository.findByUsername(restoredUsername)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Username gốc đã được sử dụng, không thể khôi phục tự động.");
                });

        userRepository.findByEmail(restoredEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Email gốc đã được sử dụng, không thể khôi phục tự động.");
                });

        user.setUsername(restoredUsername);
        user.setEmail(restoredEmail);
        user.setIsDeleted(false);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    public void updateActiveStatus(Long id, boolean isActive) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(isActive);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void resetAllActiveStatuses() {
        userRepository.resetAllActiveStatuses();
    }

    private String stripDeletedSuffix(String value) {
        if (value == null) {
            return null;
        }
        return DELETED_SUFFIX_PATTERN.matcher(value).replaceFirst("");
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
