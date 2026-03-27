package com.english.learning.service;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Service
public class OpenAiAudioService {

    // Trỏ tới Local Whisper Python Server (cổng 5000)
    private final String LOCAL_WHISPER_URL = "http://127.0.0.1:5000/transcribe";
    private final RestTemplate restTemplate = new RestTemplate();

    public String transcribeAudio(MultipartFile audioFile) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        // Không sử dụng Bearer Token nữa
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Pass original filename so file extension is retained
        ByteArrayResource audioResource = new ByteArrayResource(audioFile.getBytes()) {
            @Override
            public String getFilename() {
                String originalFilename = audioFile.getOriginalFilename();
                return (originalFilename != null && !originalFilename.isEmpty()) ? originalFilename : "audio.webm";
            }
        };

        // Flask API expect 'file' parameter
        body.add("file", audioResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(LOCAL_WHISPER_URL, requestEntity, Map.class);

        if (response != null && response.containsKey("text")) {
            return (String) response.get("text");
        }
        return "";
    }
}
