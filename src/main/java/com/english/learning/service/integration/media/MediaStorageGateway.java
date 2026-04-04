package com.english.learning.service.integration.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface MediaStorageGateway {
    Map<String, String> uploadFile(MultipartFile file) throws IOException;

    Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder) throws IOException;

    Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder, String publicId, boolean overwrite) throws IOException;

    Map<String, String> uploadAudio(byte[] audioData, String publicId) throws IOException;

    void deleteFile(String publicId) throws Exception;
}

