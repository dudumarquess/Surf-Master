package com.surfmaster.service.rag;

import java.util.List;

public record RagContext(
        List<RagSpot> spots,
        String fallbackReason,
        Long preferredSpotId
) {

    public boolean hasContext() {
        return spots != null && !spots.isEmpty();
    }

    public boolean usedFallback() {
        return fallbackReason != null && !fallbackReason.isBlank();
    }

    public String buildContextBlock() {
        if (!hasContext()) {
            return "No surf spots found for this question.";
        }
        StringBuilder sb = new StringBuilder();
        if (preferredSpotId != null) {
            sb.append("Session focus spot (ID ").append(preferredSpotId).append("). Treat this spot as primary if it appears in the context.\n");
        }
        for (int i = 0; i < spots.size(); i++) {
            RagSpot spot = spots.get(i);
            sb.append("Spot ").append(i + 1).append(": ").append(spot.name()).append("\n");
            sb.append("  Recommended level: ").append(spot.level()).append("\n");
            sb.append("  Ideal swell: ").append(spot.swellDirection()).append("\n");
            sb.append("  Ideal wind: ").append(spot.windDirection()).append("\n");
            if (spot.notes() != null && !spot.notes().isBlank()) {
                sb.append("  Notes: ").append(spot.notes()).append("\n");
            }
        }
        return sb.toString();
    }
}
