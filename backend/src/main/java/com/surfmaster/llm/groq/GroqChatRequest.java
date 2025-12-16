package com.surfmaster.llm.groq;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqChatRequest(
        String model,
        List<Message> messages,
        double temperature,
        @JsonProperty("max_tokens") int maxTokens
) {
    public record Message(
            String role,
            String content
    ) {}
}
