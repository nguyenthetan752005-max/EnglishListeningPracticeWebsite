package com.english.learning.service;

import com.english.learning.entity.User;

import java.util.Optional;

public interface UserService {
    User register(User user);
    Optional<User> authenticate(String username, String password);
    Optional<User> findById(Long id);
}
