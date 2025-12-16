package com.surfmaster.dto;

import com.surfmaster.entities.Direction;
import com.surfmaster.entities.ForecastSource;

import java.time.OffsetDateTime;

public record ForecastDto(
        Long id,
        Long spotId,
        OffsetDateTime timestamp,
        Double swellHeight,
        Integer swellPeriod,
        Direction swellDirection,
        Double windSpeed,
        Direction windDirection,
        Double tideHeight,
        Integer waterTemperature,
        ForecastSource dataSource
) {}
