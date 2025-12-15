package com.surfmaster.dto;

import com.surfmaster.entities.ForecastWindow;

import java.time.OffsetDateTime;
import java.util.List;

public record SurfSummaryDto(
        Long id,
        Long spotId,
        OffsetDateTime generatedAt,
        ForecastWindow window,
        String summary,
        Integer score,
        List<String> tags
) {
}
