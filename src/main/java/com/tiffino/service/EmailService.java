package com.tiffino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${mail.api.key}")
    private String MAIL_API_KEY;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    @Async
    public void sendEmail(String to, String subject, String message) {
        Future<String> future = executorService.submit(() -> {

            if (!isDeliverableEmail(to)) {
                log.warn("Email validation failed, but allowing send to {}", to);
            }

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(message);
            javaMailSender.send(email);

            return "Email successfully sent to " + to;
        });

        try {
            String result = future.get();
            log.info(result);
        } catch (Exception e) {
            log.error("Error sending email", e);
        }
    }

    public boolean isDeliverableEmail(String email) {
        try {
            String url = String.format(
                    "http://apilayer.net/api/check?access_key=%s&email=%s&smtp=1&format=1",
                    MAIL_API_KEY,
                    URLEncoder.encode(email, StandardCharsets.UTF_8)
            );

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(new java.net.URI(url))
                    .GET()
                    .build();

            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(res.body());

            log.info("MailboxLayer response: {}", json.toString());

            boolean formatValid = json.path("format_valid").asBoolean();
            boolean mxFound = json.path("mx_found").asBoolean();

            boolean smtpCheck = json.path("smtp_check").asBoolean();

            boolean isValid = formatValid && mxFound;

            if (json.has("error")) {
                log.warn("API returned error, allowing email as valid: {}", json.path("error"));
                return true;
            }

            if (!isValid) {
                log.warn("Email validation failed, smtp_check={} (ignored)", smtpCheck);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying email, allowing as valid", e);
            return true;
        }
    }

    @Async
    public void sendOtpEmail(String to, int otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        javaMailSender.send(message);
    }
}