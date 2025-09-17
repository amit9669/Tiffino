package com.tiffino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageUploadService {

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";
    private static final String API_KEY = "4b13c7f9a6d506df9b7988d9dd2db7eb";

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            System.out.println("No file provided");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("key", API_KEY);
            body.add("image", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    IMGBB_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            System.out.println("Response Body: " + response.getBody());
            return parseImageUrlFromResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseImageUrlFromResponse(String response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response);
        return jsonResponse.path("data").path("url").asText(null);
    }
}
