package com.tiffino.service;

import com.tiffino.config.SimpleClassifier;
import com.tiffino.entity.Order;
import com.tiffino.entity.OrderComplaint;
import com.tiffino.entity.User;
import com.tiffino.entity.request.ClientMessage;
import com.tiffino.entity.response.BotResponse;
import com.tiffino.repository.OrderComplaintRepository;
import com.tiffino.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatBotService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderComplaintRepository orderComplaintRepository;

    @Autowired
    private ImageUploadService imageUploadService;


    private static final Logger log = LoggerFactory.getLogger(ChatBotService.class);

    private final SimpleClassifier classifier = new SimpleClassifier();

    // ---------------- WELCOME MESSAGE ----------------

    public BotResponse buildWelcome() {
        BotResponse r = BotResponse.of(
                "Hi — I’m FoodCare. I can help with order mismatches, spoiled food, insects, bad taste and more. What happened with your order?",
                "welcome"
        );
        r.setQuickReplies(Arrays.asList("Wrong item delivered", "Food spoiled", "Found insect", "Bad taste"));
        return r;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    // ---------------- MESSAGE HANDLER ----------------


    public BotResponse handleClientMessage(String sessionId, ClientMessage client, Map<String, Object> metadata) {
        String text = safe(client.getMessage());

        // Case 1: user uploaded a photo (MultipartFile present)
        if (client.getFoodUrl() != null && !client.getFoodUrl().isEmpty()) {
            return handleImageUpload(sessionId, client, metadata);
        }

        // 🟢 Case 2: check if user previously was asked about spoiled food details
        if (Boolean.TRUE.equals(metadata.get("expectingSpoiledType"))) {
            return handleSpoiled(client, metadata, null);
        }

        // Otherwise, do classification as before
        SimpleClassifier.Classification classification = classifier.classify(text);
        log.info("Session {} classification: {}", sessionId, classification);
        metadata.put("lastClassification", classification);

        switch (classification.type) {
            case MISMATCH:
                return handleMismatch(client, metadata, classification);
            case SPOILED:
                return handleSpoiled(client, metadata, classification);
            case INSECT:
                return handleInsect(client, metadata, classification);
            case UNKNOWN:
            default:
                return BotResponse.of("I'm sorry — can you tell me more? For example: 'My order arrived cold' or 'I found an insect in the curry' or 'I got a completely different dish'.", "ask_details");
        }
    }

    // ---------------- IMAGE UPLOAD HANDLER ----------------

    private BotResponse handleImageUpload(String sessionId, ClientMessage client, Map<String, Object> metadata) {
        SimpleClassifier.Classification last = (SimpleClassifier.Classification) metadata.get("lastClassification");

        if (last == null) {
            return BotResponse.of(
                    "I received your image, but I’m not sure what it’s about. Could you please describe the issue?",
                    "ask_context"
            );
        }

        String imageUrl = client.getFoodUrl();
        log.info("✅ Image uploaded for session {}: {}", sessionId, imageUrl);

        // 🟢 Notify manager / save complaint to DB
        saveComplaintForManager(client, last, imageUrl);

        // 🟢 Build confirmation reply for user
        String confirmation;
        if (last.type == SimpleClassifier.Type.INSECT) {
            confirmation = "Thank you. Your photo has been uploaded and forwarded to our quality team for urgent review.";
        } else if (last.type == SimpleClassifier.Type.MISMATCH) {
            confirmation = "Got it! The image of your wrong item has been shared with our support team for verification.";
        } else {
            confirmation = "Thanks for the image — our team will review it shortly.";
        }

        BotResponse response = BotResponse.of(confirmation, "image_received");
        response.setQuickReplies(Arrays.asList("Okay", "Talk to support"));
        return response;
    }

    // ---------------- SAVE COMPLAINT FOR MANAGER ----------------

    private void saveComplaintForManager(ClientMessage client, SimpleClassifier.Classification classification, String imageUrl) {
        try {
            OrderComplaint complaint = new OrderComplaint();
            complaint.setComplaintText(classification.type.toString());
            complaint.setImageUrl(imageUrl);
            complaint.setOrderId(client.getOrderId());

            Order order = orderRepository.findById(client.getOrderId()).get();

            complaint.setUserId(order.getUser().getUserId());

            orderComplaintRepository.save(complaint);

            Map<String, Object> record = new HashMap<>();
            record.put("orderId", client.getOrderId());
            record.put("type", classification.type.toString());
            record.put("message", client.getMessage());
            record.put("imageUrl", imageUrl);
            record.put("status", "PENDING_REVIEW");

            log.info("📩 Manager notified with complaint: {}", record);
        } catch (Exception e) {
            log.error("❌ Failed to notify manager", e);
        }
    }

    // ---------------- HANDLERS FOR EACH TYPE ----------------

    private BotResponse handleMismatch(ClientMessage client, Map<String, Object> meta, SimpleClassifier.Classification c) {
        meta.put("expectingPhoto", true);
        String suggested = "I understand the wrong item arrived. Can you upload a photo of the meal you received? If you don't want to share, reply with 'no'.";
        BotResponse r = BotResponse.of(suggested, "ask_for_photo");
        r.setQuickReplies(Arrays.asList("Upload photo", "No photo"));
        return r;
    }

    private BotResponse handleSpoiled(ClientMessage client, Map<String, Object> meta, SimpleClassifier.Classification c) {
        String userReply = safe(client.getMessage()).toLowerCase();

        // If we're expecting user to specify spoiled type (visibly spoiled / bad smell / cold)
        if (Boolean.TRUE.equals(meta.get("expectingSpoiledType"))) {
            meta.remove("expectingSpoiledType"); // clear state

            // Save complaint in DB — no image, just text
            SimpleClassifier.Classification classification =
                    (c != null) ? c : new SimpleClassifier.Classification(SimpleClassifier.Type.SPOILED, 1.0, List.of());
            saveComplaintForManager(client, classification, null);

            if (userReply.contains("visibly") || userReply.contains("spoiled") || userReply.contains("bad smell")) {
                return BotResponse.of(
                        "Sorry for that, you will get a response from our technical support team shortly.",
                        "notify_support"
                );
            } else if (userReply.contains("arrived") || userReply.contains("cold")) {
                return BotResponse.of(
                        "Extremely sorry for that. We’ll ensure your next order is delivered fresh and warm!",
                        "apologize_cold"
                );
            } else {
                BotResponse retry = BotResponse.of(
                        "Please choose one option: (a) visibly spoiled, (b) bad smell, or (c) arrived cold.",
                        "ask_spoiled_type_retry"
                );
                retry.setQuickReplies(Arrays.asList("Visibly spoiled", "Bad smell", "Arrived cold"));
                return retry;
            }
        }

        // Ask user for more details
        meta.put("expectingSpoiledType", true);

        BotResponse r = BotResponse.of(
                "Sorry about that. Was the food: (a) visibly spoiled/rotten, (b) had bad smell, or (c) arrived cold? Reply with one option.",
                "ask_spoiled_type"
        );
        r.setQuickReplies(Arrays.asList("Visibly spoiled", "Bad smell", "Arrived cold"));
        return r;
    }



    private BotResponse handleInsect(ClientMessage client, Map<String, Object> meta, SimpleClassifier.Classification c) {
        meta.put("expectingPhoto", true);
        BotResponse r = BotResponse.of(
                "I’m really sorry. Please send a clear photo of the item (if possible) and confirm whether the food was consumed. We will escalate immediately and initiate investigation.",
                "ask_for_photo"
        );
        Map<String, Object> details = new HashMap<>();
        details.put("escalation", "high");
        r.setDetails(details);
        r.setQuickReplies(Arrays.asList("Upload photo"));
        log.warn("⚠️ Escalation required: insect found — mark session for human review. Classification details: {}", c);
        meta.put("escalated", true);
        return r;
    }
}