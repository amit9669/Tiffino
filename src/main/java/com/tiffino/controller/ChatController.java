package com.tiffino.controller;

import com.tiffino.entity.request.ClientMessage;
import com.tiffino.entity.response.BotResponse;
import com.tiffino.service.ChatBotService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatBotService chatBotService;

    public ChatController(ChatBotService chatBotService) {
        this.chatBotService = chatBotService;
    }

    @PostMapping("/message")
    public BotResponse handleChat(@RequestBody ClientMessage message) {
        return chatBotService.handleClientMessage("rest-api", message, new HashMap<>());
    }
}

