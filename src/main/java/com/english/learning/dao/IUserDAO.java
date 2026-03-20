package com.english.learning.dao;

import com.english.learning.model.User;

import java.util.Optional;

public interface IUserDAO {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    User save(User user);
}
