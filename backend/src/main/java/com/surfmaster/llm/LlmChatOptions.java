package com.surfmaster.llm;

import lombok.Builder;

@Builder
public record LlmChatOptions(
        double temperature,
        int maxTokens
) {
}
