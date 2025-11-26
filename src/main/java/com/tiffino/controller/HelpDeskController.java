package com.tiffino.controller;

import com.tiffino.entity.request.HelpDeskRequest;
import com.tiffino.entity.request.HelpDeskResponse;
import com.tiffino.service.HelpDeskChatbotAgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/helpdesk")
@RequiredArgsConstructor
public class HelpDeskController {

    private final HelpDeskChatbotAgentService helpDeskChatbotAgentService;

    @PostMapping("/chat")
    public ResponseEntity<HelpDeskResponse> chat(@RequestBody HelpDeskRequest helpDeskRequest) {
        String chatResponse = helpDeskChatbotAgentService
                .call(helpDeskRequest.getPromptMessage(), helpDeskRequest.getHistoryId());

        return new ResponseEntity<>(new HelpDeskResponse(chatResponse), HttpStatus.OK);
    }
}