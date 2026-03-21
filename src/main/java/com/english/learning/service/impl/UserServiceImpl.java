package com.english.learning.service.impl;

import com.english.learning.repository.UserRepository;
import com.english.learning.entity.User;
import com.english.learning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        // TODO: Hash password trước khi lưu
        return userRepository.save(user);
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // TODO: So sánh password đã hash
            if (user.getPassword().equals(password)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
