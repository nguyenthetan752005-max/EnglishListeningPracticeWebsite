package com.english.learning.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.MappingIterator;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

@Service
public class WitAIAudioService {

    // Đọc URL từ application.properties
    @Value("${wit.api.url}")
    private String WIT_AI_URL;

    // Đọc Token từ application.properties
    @Value("${wit.api.token}")
    private String WIT_AI_TOKEN;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        // Xác thực qua Bearer Token
        headers.set("Authorization", "Bearer " + WIT_AI_TOKEN);

        // Khai báo thẳng với Wit.ai đây là file WAV chuẩn
        headers.set("Content-Type", "audio/wav");

        // Chuyển file audio thành byte mảng để đẩy trực tiếp qua HTTP
        byte[] audioBytes = audioFile.getBytes();
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(audioBytes, headers);

        // Gửi yêu cầu POST tới Wit.ai
        ResponseEntity<String> response = restTemplate.exchange(
                WIT_AI_URL,
                HttpMethod.POST,
                requestEntity,
                String.class);

        String responseBody = response.getBody();
        String transcribedText = "";

        if (responseBody != null && !responseBody.trim().isEmpty()) {
            try {
                // Dùng MappingIterator của Jackson để tự động bóc tách các Object JSON dính
                // liền nhau
                MappingIterator<JsonNode> iterator = mapper.readerFor(JsonNode.class)
                        .readValues(responseBody);

                System.out.println("=== DEBUG: Raw Response from Wit.ai ===");
                System.out.println(responseBody);
                System.out.println("======================================");

                while (iterator.hasNextValue()) {
                    JsonNode jsonNode = iterator.nextValue();

                    // CHỈ CẬP NHẬT TEXT NẾU NÓ KHÔNG BỊ RỖNG
                    if (jsonNode.has("text")) {
                        String tempText = jsonNode.get("text").asText();
                        if (tempText != null && !tempText.trim().isEmpty()) {
                            transcribedText = tempText;
                        }
                    }

                    // Nếu gặp cờ is_final = true thì dừng (đã có kết quả cuối cùng)
                    if (jsonNode.has("is_final") && jsonNode.get("is_final").asBoolean()) {
                        break;
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi parse JSON từ Wit.ai: " + e.getMessage());
                System.err.println("Nội dung raw từ Wit.ai:\n" + responseBody);
                throw e; // Ném lỗi ra để Controller bắt và báo về UI
            }
        }

        return transcribedText.trim();
    }
}
