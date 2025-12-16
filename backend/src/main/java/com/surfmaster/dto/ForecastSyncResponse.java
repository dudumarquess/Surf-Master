package com.surfmaster.dto;

import java.util.List;

public record ForecastSyncResponse(
        int refreshedSpots,
        int totalForecastsSaved,
        List<ForecastDto> forecasts
) {}
