package com.tiffino.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffino.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${mail.api.key}")
    private String MAIL_API_KEY;

    public void sendEmail(String to, String subject, String message) {
        try {
            if (!isDeliverableEmail(to)) {
                log.warn("Invalid or undeliverable email: {}", to);
                return;
            }

            SimpleMailMessage email = new SimpleMailMessage();
            email.setTo(to);
            email.setSubject(subject);
            email.setText(message);
            javaMailSender.send(email);

            log.info("Email successfully sent to {}", to);

        } catch (CustomException e) {
            log.error("Exception while sending Email", e);
        }
    }


    private final AtomicInteger apiUsageCounter = new AtomicInteger(0);
    private LocalDate lastResetDate = LocalDate.now();

    public boolean isDeliverableEmail(String email) {
        try {

            LocalDate now = LocalDate.now();
            if (!now.getMonth().equals(lastResetDate.getMonth())) {
                apiUsageCounter.set(0);
                lastResetDate = now;
            }

            int currentUsage = apiUsageCounter.incrementAndGet();

            int API_LIMIT = 1000;
            if (currentUsage > API_LIMIT) {
                log.warn("MailboxLayer API monthly limit reached!");
                throw new RuntimeException("MailboxLayer API limit expired for this month");
            }

            String apiKey = MAIL_API_KEY;

            String url = String.format(
                    "http://apilayer.net/api/check?access_key=%s&email=%s&smtp=1&format=1",
                    apiKey,
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

            if (json.has("success") && !json.get("success").asBoolean()) {
                String errorType = json.path("error").path("type").asText();
                if ("usage_limit_reached".equals(errorType)) {
                    throw new RuntimeException("MailboxLayer API limit expired (from provider)");
                }
            }

            boolean formatValid = json.path("format_valid").asBoolean();
            boolean mxFound = json.path("mx_found").asBoolean();
            boolean smtpCheck = json.path("smtp_check").asBoolean();

            log.info("MailboxLayer response: {}", json.toString());

            return formatValid && mxFound && smtpCheck;

        } catch (Exception e) {
            log.error("Error verifying email with MailboxLayer", e);
            return false;
        }
    }

    public void sendOtpEmail(String to, int otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp);
        javaMailSender.send(message);
    }
}