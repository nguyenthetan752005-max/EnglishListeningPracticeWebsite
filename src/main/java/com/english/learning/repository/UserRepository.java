package com.english.learning.repository;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findFirstByRole(Role role);

    // Queries for the Leaderboard
    List<User> findTop30ByRoleOrderByActiveTime7dDesc(Role role);
    List<User> findTop30ByRoleOrderByActiveTime30dDesc(Role role);
}
