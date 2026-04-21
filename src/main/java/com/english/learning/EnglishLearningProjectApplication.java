package com.english.learning;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Properties;

import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableScheduling
@EnableCaching
@RequiredArgsConstructor
public class EnglishLearningProjectApplication {

    private final Environment env;

    public static void main(String[] args) {
        Properties properties = new Properties();
        configureAvailablePort(properties);

        SpringApplication app = new SpringApplication(EnglishLearningProjectApplication.class);
        // Cách chuẩn của Spring Boot để set property mặc định trước khi chạy
        app.setDefaultProperties(properties); 
        app.run(args);
    }

    private static void configureAvailablePort(Properties props) {
        int port = 8080;
        try (InputStream input = EnglishLearningProjectApplication.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties tempProps = new Properties();
                tempProps.load(input);
                port = Integer.parseInt(tempProps.getProperty("server.port", "8080"));
            }
        } catch (IOException ignored) {}

        if (!isPortAvailable(port)) {
            System.out.println("⚠️ Cổng " + port + " đang bận. Đang tìm cổng khác...");
            while (!isPortAvailable(port)) port++;
            System.out.println("✅ Chuyển sang sử dụng cổng trống: " + port);
        }
        props.put("server.port", String.valueOf(port));
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket ignored = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = env.getProperty("server.port", "8080");
        String ctx = env.getProperty("server.servlet.context-path", "");
        String localIp = getLocalIpAddress();

        System.out.println("\n=======================================================");
        System.out.println("🚀 Ứng dụng English Learning Website đã khởi động!");
        System.out.println("🌍 Web UI:         http://localhost:" + port + ctx);
        System.out.println("📱 Bootstrap:      http://localhost:" + port + ctx + "/api/mobile/bootstrap");
        System.out.println("🤖 Android (emu):  http://10.0.2.2:" + port + ctx + "/api/mobile/catalog/bootstrap-lite");
        System.out.println("📲 Android (LAN):  http://" + localIp + ":" + port + ctx + "/api/mobile/catalog/bootstrap-lite");
        System.out.println("=======================================================\n");

        startNgrok(port);
    }

    private void startNgrok(String port) {
        String token = env.getProperty("ngrok.auth-token");
        String domain = env.getProperty("ngrok.domain");
        String path = env.getProperty("ngrok.path", "ngrok");

        if (token == null || domain == null) {
            System.out.println("⚠️ Bỏ qua ngrok: Chưa cấu hình auth-token hoặc domain tĩnh.");
            return;
        }

        String executable = findNgrokExecutable(path);
        System.out.println("🌐 Đang khởi chạy ngrok trên cổng " + port + " (Domain: " + domain + ")");

        try {
            String os = System.getProperty("os.name").toLowerCase();
            String cmd = executable + " config add-authtoken " + token + " && " + executable + " http --domain=" + domain + " " + port;
            ProcessBuilder builder;

            if (os.contains("win")) {
                builder = new ProcessBuilder("cmd.exe", "/c", "start", "cmd.exe", "/k", cmd);
            } else if (os.contains("mac")) {
                builder = new ProcessBuilder("osascript", "-e", "tell application \"Terminal\" to do script \"" + cmd + "\"");
            } else {
                builder = new ProcessBuilder("gnome-terminal", "--", "bash", "-c", cmd + "; exec bash");
            }
            builder.start();
        } catch (IOException e) {
            System.err.println("❌ Lỗi khởi chạy ngrok: " + e.getMessage());
        }
    }

    private String findNgrokExecutable(String defaultPath) {
        if (defaultPath.contains(File.separator) && Files.exists(Path.of(defaultPath))) return defaultPath;

        String os = System.getProperty("os.name").toLowerCase();
        // Gọi thẳng qua cmd/bash để mượn khả năng tự phân giải PATH của hệ điều hành
        String[] searchCmd = os.contains("win") ? 
                new String[]{"cmd.exe", "/c", "where " + defaultPath} : 
                new String[]{"bash", "-c", "which " + defaultPath};

        try {
            Process process = new ProcessBuilder(searchCmd).start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.trim().isEmpty()) return line.trim();
            }
        } catch (Exception ignored) {}

        String[] commonPaths = {
                System.getenv("LOCALAPPDATA") + "\\ngrok\\ngrok.exe",
                System.getenv("USERPROFILE") + "\\ngrok.exe",
                "/usr/local/bin/ngrok",
                "/opt/ngrok/ngrok"
        };

        for (String p : commonPaths) {
            if (p != null && Files.exists(Path.of(p))) return p;
        }
        return defaultPath;
    }

    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> itfs = NetworkInterface.getNetworkInterfaces(); itfs.hasMoreElements();) {
                NetworkInterface ni = itfs.nextElement();
                if (ni.isLoopback() || !ni.isUp()) continue;
                
                for (Enumeration<InetAddress> addrs = ni.getInetAddresses(); addrs.hasMoreElements();) {
                    InetAddress addr = addrs.nextElement();
                    String ip = addr.getHostAddress();
                    if (ip.contains(".") && !ip.startsWith("127.")) return ip;
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}