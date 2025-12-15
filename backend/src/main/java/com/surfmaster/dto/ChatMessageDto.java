package com.surfmaster.dto;

import com.surfmaster.entities.ChatRole;
import java.time.OffsetDateTime;

public record ChatMessageDto(
        Long id,
        ChatRole chatRole,
        Long chatSessionId,
        String content,
        OffsetDateTime createdAt
) {}
