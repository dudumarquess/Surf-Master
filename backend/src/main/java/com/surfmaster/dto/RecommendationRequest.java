package com.surfmaster.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.surfmaster.entities.Objective;
import com.surfmaster.entities.UserLevel;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record RecommendationRequest(

    @NotNull
    @DecimalMin(value = "-180.0", inclusive = true)
    @DecimalMax(value = "180.0", inclusive = true)
    Double longitude,

    @NotNull
    @DecimalMin(value = "-90.0", inclusive = true)
    @DecimalMax(value = "90.0", inclusive = true)
    Double latitude,
    
    UserLevel userLevel,
    Objective objective,

    @NotNull
    @DecimalMin(value = "0.1", inclusive = true)
    @DecimalMax(value = "200.0", inclusive = true)
    Double maxDistanceKm,

    OffsetDateTime timeStart,
    OffsetDateTime timeEnd,
    Integer topK,
    Integer minWindowHours
) {}

