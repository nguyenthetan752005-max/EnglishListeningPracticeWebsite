package com.english.learning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class EnglishLearningProjectApplication {

    @Autowired
    private Environment env;

    public static void main(String[] args) {
        SpringApplication.run(EnglishLearningProjectApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printApplicationUrl() {
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");

        System.out.println("\n=======================================================");
        System.out.println("ỨNG DỤNG ĐÃ KHỞI ĐỘNG THÀNH CÔNG!");
        System.out.println("Click vào link này để mở web: http://localhost:" + port + contextPath);
        System.out.println("=======================================================\n");
    }
}
