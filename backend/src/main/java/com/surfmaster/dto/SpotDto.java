package com.surfmaster.dto;

import com.surfmaster.entities.Direction;
import com.surfmaster.entities.UserLevel;

import java.util.List;

public record SpotDto(
        Long id,
        String name,
        Double longitude,
        Double latitude,
        Direction swellBestDirection,
        Direction windBestDirection,
        UserLevel recommendedLevel,
        List<String> notes
) {
}
