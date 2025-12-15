package com.surfmaster.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ChatSessionDto(
        Long id,
        Long userId,
        Long spotId,
        OffsetDateTime createdAt,
        List<ChatMessageDto> messages
) {}
