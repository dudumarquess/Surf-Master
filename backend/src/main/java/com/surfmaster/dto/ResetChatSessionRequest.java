package com.surfmaster.dto;

import jakarta.validation.constraints.NotNull;

public record ResetChatSessionRequest(
        @NotNull Long spotId,
        Long userId
) {}
