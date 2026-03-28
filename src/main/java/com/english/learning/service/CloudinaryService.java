package com.english.learning.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    @SuppressWarnings("rawtypes")
    public String uploadFile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("secure_url").toString();
    }

    /**
     * Upload audio lên Cloudinary với public_id cố định (để ghi đè khi nói lại).
     * resource_type = "auto" để Cloudinary tự nhận dạng audio.
     * overwrite = true để ghi đè file cũ cùng public_id.
     */
    @SuppressWarnings("rawtypes")
    public String uploadAudio(byte[] audioData, String publicId) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(audioData, ObjectUtils.asMap(
                "resource_type", "auto",
                "public_id", publicId,
                "overwrite", true,
                "folder", "speaking_audio"
        ));
        return uploadResult.get("secure_url").toString();
    }
}
