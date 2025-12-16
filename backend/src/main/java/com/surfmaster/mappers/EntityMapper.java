package com.surfmaster.mappers;

import com.surfmaster.dto.*;
import com.surfmaster.entities.*;

public class EntityMapper {

    public static SpotDto toDto(Spot spot) {
        return new SpotDto(
                spot.getId(),
                spot.getName(),
                spot.getLongitude(),
                spot.getLatitude(),
                spot.getSwellBestDirection(),
                spot.getWindBestDirection(),
                spot.getRecommendedLevel(),
                spot.getNotes()
        );
    }

    public static ForecastDto toDto(Forecast f) {
        return new ForecastDto(
                f.getId(),
                f.getSpot() != null ? f.getSpot().getId() : null,
                f.getTimestamp().toInstant().atOffset(java.time.ZoneOffset.UTC),
                f.getSwellHeight(),
                f.getSwellPeriod(),
                f.getSwellDirection(),
                f.getWindSpeed(),
                f.getWindDirection(),
                f.getTideHeight(),
                f.getWaterTemperature(),
                f.getDataSource()
        );
    }

    public static SurfSummaryDto toDto(SurfSummary s) {
        return new SurfSummaryDto(
                s.getId(),
                s.getSpot() != null ? s.getSpot().getId() : null,
                s.getGeneratedAt(),
                s.getWindow(),
                s.getSummary(),
                s.getScore(),
                s.getTags()
        );
    }

    public static UserProfileDto toDto(UserProfile u) {
        return new UserProfileDto(
                u.getId(),
                u.getDisplayName(),
                u.getLevel(),
                u.getPreferredBoards()
        );
    }
}
