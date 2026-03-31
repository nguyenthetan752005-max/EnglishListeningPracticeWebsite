package com.english.learning.repository;

import com.english.learning.entity.PasswordResetToken;
import com.english.learning.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByExpiryDateBefore(java.time.LocalDateTime expiryDate);
}
