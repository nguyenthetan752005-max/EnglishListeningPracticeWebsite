package com.english.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class EnglishLearningProjectApplication {

    private final Environment env;

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(EnglishLearningProjectApplication.class);

        Properties properties = new Properties();
        setPort(properties);

        app.run(args);
    }

    private static void setPort(Properties properties) {
        try (InputStream input = EnglishLearningProjectApplication.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            properties.load(input);
            int port = Integer.parseInt(properties.getProperty("server.port", "8080"));
            if (!isPortAvailable(port)) {
                System.out.println("Cổng " + port + " đã bị sử dụng. Thử dùng cổng khác ...");
                while (!isPortAvailable(port)) {
                    port++;
                }
                System.out.println("Đã tìm thấy cổng " + port + " trống. Chương trình sẽ sử dụng cổng này!");
                System.setProperty("server.port", String.valueOf(port));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
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
