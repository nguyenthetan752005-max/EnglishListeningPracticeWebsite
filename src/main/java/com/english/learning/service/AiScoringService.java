package com.english.learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import com.english.learning.entity.SpeakingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiScoringService {

    // 1. URL chính thức của Groq API
    @Value("${groq.api.url}")
    private String GROQ_API_URL;

    // ĐIỀN API KEY CỦA BẠN VÀO ĐÂY (Trong thực tế nên để ở file
    // application.properties)
    @Value("${groq.api.key}")
    private String GROQ_API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SpeakingResult scoreSpeaking(String expectedText, String userText) {
        try {
            // 2. Setup Header (Sử dụng Bearer Token)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(GROQ_API_KEY);

            // Giới hạn số chữ trong lời khuyên để AI chạy nhanh hơn nữa
            // Giới hạn số chữ và BỔ SUNG THANG ĐIỂM (Rubric) RÕ RÀNG
            String systemPrompt = "You are an English pronunciation grading API. Compare the 'expected text' and 'user text'. "
                    + "SCORING RUBRIC: Calculate the score strictly based on the percentage of matched words. "
                    + "(Number of correctly spoken words / Total expected words) * 100. "
                    + "Example: If 2 out of 3 words are correct, the score must be around 66. "
                    + "Ignore minor STT formatting errors ('10' vs 'ten', 'where\\'s' vs 'where is'). "
                    + "Return ONLY a valid JSON object with keys: 'score' (int 0-100), 'explanation' (string pointing out the exact mispronounced word, max 10 words), and 'advice' (string, max 10 words). STRICTLY JSON ONLY.";
            String userPrompt = "Expected text: '" + expectedText + "'. User text: '" + userText + "'";

            // 3. Xây dựng Body gửi đi
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama-3.1-8b-instant"); // Model Llama 3 8B cực kỳ thông minh và siêu tốc
            requestBody.put("stream", false);

            // Ép AI trả lời máy móc, chuẩn xác, không tự ý sáng tạo bay bổng
            requestBody.put("temperature", 0.0);

            // TÍNH NĂNG ĐẶC BIỆT: Ép API của Groq bắt buộc phải nhả ra định dạng JSON
            requestBody.put("response_format", Map.of("type", "json_object"));

            requestBody.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userPrompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 4. Gửi Request
            ResponseEntity<String> response = restTemplate.postForEntity(GROQ_API_URL, entity, String.class);

            // 5. Đọc kết quả (Cấu trúc của Groq/OpenAI nằm trong mảng choices)
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            String aiResponseContent = rootNode.path("choices").get(0).path("message").path("content").asText();

            // 6. Parse thẳng ra Object (Không cần hàm cleanJsonResponse nữa vì đã dùng
            // response_format)
            JsonNode scoreNode = objectMapper.readTree(aiResponseContent);
            int score = scoreNode.path("score").asInt(0);
            String explanation = scoreNode.path("explanation").asText("No explanation");
            String advice = scoreNode.path("advice").asText("Keep practicing!");

            SpeakingResult result = new SpeakingResult();
            result.setScore(score);
            result.setFeedback(explanation + " " + advice);
            return result;

        } catch (Exception e) {
            System.err.println("Lỗi gọi Groq AI: " + e.getMessage());
            SpeakingResult result = new SpeakingResult();
            result.setScore(0);
            result.setFeedback("System error Please try again later.");
            return result;
        }
    }
}