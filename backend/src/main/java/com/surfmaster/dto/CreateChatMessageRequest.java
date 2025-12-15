package com.surfmaster.dto;

import com.surfmaster.entities.ChatRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateChatMessageRequest(
        @NotNull Long chatSessionId,
        @NotNull ChatRole chatRole,
        @NotBlank String content
) {}
