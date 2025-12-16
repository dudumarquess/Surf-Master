package com.surfmaster.dto;

import java.time.LocalDateTime;

public record RecommendationResponse(
    LocalDateTime generatedAt,
    LocalDateTime timeStart,
    LocalDateTime timeEnd,
    java.util.List<RecommendationItem> recommendations
) {}
