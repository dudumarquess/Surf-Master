package com.surfmaster.llm;

public record LlmCompletionResult(
        String content,
        double tokenCost
) {}
