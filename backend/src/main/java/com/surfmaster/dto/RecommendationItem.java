package com.surfmaster.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.surfmaster.entities.Reason;
import com.surfmaster.entities.Risk;

public record RecommendationItem(
    Long spotId,
    String spotName,
    OffsetDateTime bestWindowStart,
    OffsetDateTime bestWindowEnd,
    OffsetDateTime peakTime,
    Double score,
    java.util.List<Reason> reasons,
    java.util.List<Risk> risks,
    Double confidence
) {}
