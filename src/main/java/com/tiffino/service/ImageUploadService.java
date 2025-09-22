package com.tiffino.service;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageUploadService {

    private static final String CLOUD_NAME = "dd9dcfegb";
    private static final String API_KEY    = "619271649749132";
    private static final String API_SECRET = "GGnmGLsi5fILfMoOsZ5gmHgvW3Y";

    private final Cloudinary cloudinary;

    public ImageUploadService() {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUD_NAME,
                "api_key", API_KEY,
                "api_secret", API_SECRET
        ));
    }

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            System.out.println("No file provided");
            return null;
        }

        try {
            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto" // auto-detect image/video
                    )
            );

            // secure_url is HTTPS; you can also use "url"
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
