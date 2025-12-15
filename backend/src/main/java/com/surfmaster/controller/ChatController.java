package com.surfmaster.controller;

import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.dto.CreateChatMessageRequest;
import com.surfmaster.service.ChatService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/sessions/{spotId}")
    public ChatSessionDto openSession(@PathVariable Long spotId, @RequestParam(required = false) Long userId) {
        return chatService.openSession(spotId, userId);
    }

    @GetMapping("sessions/{sessionId}")
    public ChatSessionDto getSession(@PathVariable Long sessionId) {
        return chatService.getSession(sessionId);
    }

    @PostMapping("/messages")
    public ChatMessageDto sendMessage(@RequestBody CreateChatMessageRequest message) {
        return chatService.postUserMessage(message);
    }
}
