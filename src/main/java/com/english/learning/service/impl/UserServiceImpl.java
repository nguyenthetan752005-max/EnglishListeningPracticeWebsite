package com.english.learning.service.impl;

import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import com.english.learning.entity.User;
import com.english.learning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

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
}
