package com.english.learning.service.integration.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class CloudinaryService implements MediaStorageGateway {

    private final Cloudinary cloudinary;

    @SuppressWarnings("rawtypes")
    public Map<String, String> uploadFile(MultipartFile file) throws IOException {
        return uploadFile(file, "auto", null);
    }

    @SuppressWarnings("rawtypes")
    public Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder) throws IOException {
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType == null || resourceType.isBlank() ? "auto" : resourceType);
        if (folder != null && !folder.isBlank()) {
            options.put("folder", folder);
        }
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        Map<String, String> result = new HashMap<>();
        result.put("url", uploadResult.get("secure_url").toString());
        result.put("publicId", uploadResult.get("public_id").toString());
        return result;
    }

    @SuppressWarnings("rawtypes")
    public Map<String, String> uploadFile(MultipartFile file, String resourceType, String folder, String publicId, boolean overwrite) throws IOException {
        Map<String, Object> options = new HashMap<>();
        options.put("resource_type", resourceType == null || resourceType.isBlank() ? "auto" : resourceType);
        if (folder != null && !folder.isBlank() && (publicId == null || publicId.isBlank() || !publicId.contains("/"))) {
            options.put("folder", folder);
        }
        if (publicId != null && !publicId.isBlank()) {
            options.put("public_id", publicId);
        }
        options.put("overwrite", overwrite);
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
        Map<String, String> result = new HashMap<>();
        result.put("url", uploadResult.get("secure_url").toString());
        result.put("publicId", uploadResult.get("public_id").toString());
        return result;
    }

    /**
     * Upload audio lÃªn Cloudinary vá»›i public_id cá»‘ Ä‘á»‹nh.
     */
    @SuppressWarnings("rawtypes")
    public Map<String, String> uploadAudio(byte[] audioData, String publicId) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(audioData, ObjectUtils.asMap(
                "resource_type", "auto",
                "public_id", publicId,
                "overwrite", true,
                "folder", "speaking_audio"
        ));
        Map<String, String> result = new HashMap<>();
        result.put("url", uploadResult.get("secure_url").toString());
        result.put("publicId", uploadResult.get("public_id").toString());
        return result;
    }

    /**
     * XÃ³a váº­t lÃ½ file khá»i Cloudinary
     */
    @SuppressWarnings("rawtypes")
    public void deleteFile(String publicId) throws Exception {
        if (publicId == null || publicId.isEmpty()) return;
        Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        if ("not_found".equals(result.get("result"))) {
            System.err.println("Cloudinary: File not found for publicId: " + publicId);
        } else if (!"ok".equals(result.get("result"))) {
            throw new Exception("Lá»—i xÃ³a file Cloudinary: " + result.toString());
        }
    }
}

