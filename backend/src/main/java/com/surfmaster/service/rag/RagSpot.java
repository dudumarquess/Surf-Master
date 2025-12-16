package com.surfmaster.service.rag;

import com.surfmaster.entities.Direction;
import com.surfmaster.entities.UserLevel;

public record RagSpot(
        Long spotId,
        String name,
        UserLevel level,
        Direction swellDirection,
        Direction windDirection,
        String notes,
        double similarityScore,
        double heuristicScore
) {
    public double totalScore() {
        return similarityScore * 0.7 + heuristicScore * 0.3;
    }
}
