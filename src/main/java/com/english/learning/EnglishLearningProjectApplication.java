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
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.util.Enumeration;
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
    public void printApplicationUrl(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext webServerAppCtx)) {
            return;
        }
        int port = webServerAppCtx.getWebServer().getPort();
        String contextPath = env.getProperty("server.servlet.context-path", "");

        System.out.println("\n=======================================================");
        System.out.println("ỨNG DỤNG ĐÃ KHỞI ĐỘNG THÀNH CÔNG!");
        System.out.println("Web UI:           http://localhost:" + port + contextPath);
        System.out.println("Bootstrap (v1):   http://localhost:" + port + contextPath + "/api/mobile/bootstrap");
        System.out.println("Bootstrap-Lite:   http://localhost:" + port + contextPath + "/api/mobile/catalog/bootstrap-lite");
        System.out.println("Lesson Detail:    http://localhost:" + port + contextPath + "/api/mobile/lessons/{id}");
        System.out.println("Mobile Auth:      http://localhost:" + port + contextPath + "/api/mobile/auth/login");
        System.out.println("Android (emu):    http://10.0.2.2:" + port + contextPath + "/api/mobile/catalog/bootstrap-lite");
        String localIp = getLocalIpAddress();
        System.out.println("Android (USB):    http://" + localIp + ":" + port + contextPath + "/api/mobile/catalog/bootstrap-lite");
        System.out.println("=======================================================\n");
    }

    private String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                // Bỏ qua interface loopback và disabled
                if (ni.isLoopback() || !ni.isUp()) continue;
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    String ip = addr.getHostAddress();
                    // Chỉ lấy IPv4 (không phải IPv6)
                    if (ip.contains(".") && !ip.startsWith("127.")) {
                        return ip;
                    }
                }
            }
        } catch (Exception e) {
            // Fallback nếu lỗi
        }
        return "127.0.0.1";
    }
}
