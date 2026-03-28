package com.english.learning.service;

import com.english.learning.entity.User;

import java.util.Optional;

public interface UserService {
    User register(User user);

    Optional<User> authenticate(String username, String password);

    Optional<User> findById(Long id);
    void updateUsername(Long id, String newUsername) throws Exception;
    void updateAvatarUrl(Long id, String avatarUrl) throws Exception;

    Optional<User> authenticateAdmin(String username, String password);

    Optional<User> authenticateUser(String username, String password);

    Optional<User> findByEmail(String email);

    void updatePassword(User user, String newPassword);
}
