package com.surfmaster.dto;

import java.time.OffsetDateTime;

public record RecommendationResponse(
    OffsetDateTime generatedAt,
    OffsetDateTime timeStart,
    OffsetDateTime timeEnd,
    java.util.List<RecommendationItem> recommendations
) {}
