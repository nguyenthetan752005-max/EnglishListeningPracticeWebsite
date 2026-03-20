package com.english.learning.service;

import com.english.learning.model.User;

import java.util.Optional;

public interface IUserService {
    User register(User user);
    Optional<User> authenticate(String username, String password);
    Optional<User> findById(Long id);
}
