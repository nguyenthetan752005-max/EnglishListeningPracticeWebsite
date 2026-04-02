package com.english.learning.config;

import com.english.learning.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ActiveStatusInitializer {

    private final UserService userService;

    @Bean
    public ApplicationRunner resetActiveUsersOnStartup() {
        return args -> userService.resetAllActiveStatuses();
    }
}
