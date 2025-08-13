package com.tiffino.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("key", API_KEY);
            params.add("image", base64Image);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(IMGBB_API_URL, HttpMethod.POST, requestEntity, String.class);

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
            return jsonResponse.get("data").get("url").asText();
        } catch (JsonProcessingException e) {

            e.printStackTrace();
            return null;
        }
    }
}
//class ends here
