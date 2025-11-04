package com.tiffino.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tiffino.entity.request.ClientMessage;
import com.tiffino.entity.response.BotResponse;
import com.tiffino.service.ChatBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final ChatBotService botService;

    private final Map<String, Map<String, Object>> sessionData = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatBotService botService) {
        this.botService = botService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = (String) session.getAttributes().get("username");
        log.info("✅ WebSocket connected by user: {}", username);

        sessionData.put(session.getId(), new ConcurrentHashMap<>());
        BotResponse welcome = botService.buildWelcome();
        send(session, welcome);
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            ClientMessage client = mapper.readValue(payload, ClientMessage.class);
            log.info("Received from {}: {}", session.getId(), client.getMessage());

            BotResponse response = botService.handleClientMessage(session.getId(), client, sessionData.get(session.getId()));

            send(session, response);
        } catch (Exception e) {
            log.error("Error handling message", e);
            send(session, BotResponse.error("Internal error processing message"));
        }
    }

    private void send(WebSocketSession session, BotResponse response) {
        try {
            String json = mapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed: " + session.getId());
        sessionData.remove(session.getId());
    }
}