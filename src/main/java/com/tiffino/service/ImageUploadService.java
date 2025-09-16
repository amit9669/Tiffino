package com.tiffino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class ImageUploadService {

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";
    private static final String API_KEY = "4b13c7f9a6d506df9b7988d9dd2db7eb";

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            System.out.println("ImageUploadService ");
            return null;
        }
        try {
            // Convert file to Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());

            // Prepare form data
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("key", API_KEY);
            params.add("image", base64Image);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> requestEntity =
                    new HttpEntity<>(params, headers);

            // Send request
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    IMGBB_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Parse response
            return parseImageUrlFromResponse(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String parseImageUrlFromResponse(String response) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonResponse = objectMapper.readTree(response);

            if (jsonResponse.has("data")) {
                return jsonResponse.get("data").get("url").asText();
            } else {
                System.err.println("Invalid response: " + response);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
