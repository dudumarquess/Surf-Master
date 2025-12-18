package com.surfmaster.service.rag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Spot;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.llm.EmbeddingClient;
import com.surfmaster.repository.SpotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotRagService {

    private final SpotRepository spotRepository;
    private final EmbeddingClient embeddingClient;

    private final Map<Long, double[]> spotEmbeddings = new ConcurrentHashMap<>();

    private static final Pattern SWELL_VALUE = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)");

    public RagContext retrieveContext(String question, Long preferredSpotId) {
        Spot preferredSpot = null;
        if (preferredSpotId != null) {
            preferredSpot = spotRepository.findById(preferredSpotId).orElse(null);
        }
        AtomicReference<String> fallbackReason = new AtomicReference<>();
        if (question == null || question.isBlank()) {
            log.warn("Empty RAG question, returning empty context");
            return new RagContext(List.of(), "Empty question, embeddings not generated.", preferredSpotId,
                    preferredSpot != null ? preferredSpot.getName() : null);
        }

        log.debug("RAG question='{}'", question);
        double[] queryEmbedding = safeEmbed(question, fallbackReason);
        QuerySignals signals = extractSignals(question);
        log.debug("RAG signals level={} swellHeight={} direction={}", signals.level, signals.swellHeight, signals.direction);

        List<RagSpot> ranked = spotRepository.findAll().stream()
                .map(spot -> buildRagSpot(spot, queryEmbedding, signals, fallbackReason))
                .sorted(Comparator.comparingDouble((RagSpot spot) -> scoreWithPreference(spot, preferredSpotId))
                        .reversed())
                .limit(3)
                .collect(ArrayList::new, List::add, List::addAll);

        if (preferredSpot != null) {
            RagSpot preferredRagSpot = buildRagSpot(preferredSpot, queryEmbedding, signals, fallbackReason);
            ranked.removeIf(spot -> spot.spotId().equals(preferredSpotId));
            ranked.add(0, preferredRagSpot);
            if (ranked.size() > 3) {
                ranked.subList(3, ranked.size()).clear();
            }
        }

        log.info("RAG selected {} spots", ranked.size());
        return new RagContext(
                ranked,
                fallbackReason.get(),
                preferredSpotId,
                preferredSpot != null ? preferredSpot.getName() : null
        );
    }

    private double scoreWithPreference(RagSpot spot, Long preferredSpotId) {
        double base = spot.totalScore();
        if (preferredSpotId != null && preferredSpotId.equals(spot.spotId())) {
            return base + 0.5;
        }
        return base;
    }

    private List<RagSpot> topCandidates(double[] queryEmbedding, QuerySignals signals, AtomicReference<String> fallbackReason) {
        return spotRepository.findAll().stream()
                .map(spot -> buildRagSpot(spot, queryEmbedding, signals, fallbackReason))
                .sorted(Comparator.comparingDouble(RagSpot::totalScore).reversed())
                .limit(3)
                .toList();
    }

    private RagSpot buildRagSpot(Spot spot, double[] queryEmbedding, QuerySignals signals, AtomicReference<String> fallbackReason) {
        double[] spotEmbedding = spotEmbeddings.computeIfAbsent(spot.getId(), id -> safeEmbed(describeSpot(spot), fallbackReason));
        double similarity = cosine(queryEmbedding, spotEmbedding);
        double heuristics = heuristicScore(spot, signals);
        String notes = spot.getNotes() != null ? String.join(", ", spot.getNotes()) : null;
        return new RagSpot(
                spot.getId(),
                spot.getName(),
                spot.getRecommendedLevel(),
                spot.getSwellBestDirection(),
                spot.getWindBestDirection(),
                notes,
                similarity,
                heuristics
        );
    }

    private double heuristicScore(Spot spot, QuerySignals signals) {
        double score = 0.0;
        if (signals.level != null && spot.getRecommendedLevel() == signals.level) {
            score += 1.0;
        }
        if (signals.direction != null && spot.getSwellBestDirection() == signals.direction) {
            score += 0.5;
        }
        if (signals.swellHeight != null) {
            double preferred = preferredSwellForLevel(spot.getRecommendedLevel());
            double diff = Math.abs(preferred - signals.swellHeight);
            score += Math.max(0.0, 1.0 - diff / 3.0);
        }
        return score;
    }

    private double preferredSwellForLevel(UserLevel level) {
        return switch (level) {
            case BEGINNER -> 1.5;
            case INTERMEDIATE -> 2.5;
            case ADVANCED -> 3.5;
        };
    }

    private double cosine(double[] a, double[] b) {
        if (a == null || b == null || a.length == 0 || b.length == 0) return 0.0;
        double dot = 0;
        double normA = 0;
        double normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double[] safeEmbed(String text, AtomicReference<String> fallbackReason) {
        try {
            return embeddingClient.embed(text);
        } catch (Exception e) {
            log.error("Failed to generate embedding for text '{}'. Using fallback.", text, e);
            fallbackReason.compareAndSet(null, "Could not generate BGE embeddings (HuggingFace). Using basic heuristics.");
            // fallback: deterministic hash vector
            double[] fallback = new double[16];
            for (int i = 0; i < text.length(); i++) {
                fallback[i % fallback.length] += text.charAt(i);
            }
            return fallback;
        }
    }

    private record QuerySignals(UserLevel level, Double swellHeight, Direction direction) {}

    private QuerySignals extractSignals(String question) {
        String normalized = question.toLowerCase(Locale.ROOT);

        UserLevel level = null;
        if (normalized.contains("iniciante") || normalized.contains("beginner")) {
            level = UserLevel.BEGINNER;
        } else if (normalized.contains("intermedi") || normalized.contains("medio")) {
            level = UserLevel.INTERMEDIATE;
        } else if (normalized.contains("avanca") || normalized.contains("avançado")) {
            level = UserLevel.ADVANCED;
        }

        Direction direction = Arrays.stream(Direction.values())
                .filter(dir -> normalized.contains(dir.name().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);

        Double swellHeight = null;
        if (normalized.contains("swell") || normalized.contains("onda") || normalized.contains("altura")) {
            Matcher matcher = SWELL_VALUE.matcher(question);
            if (matcher.find()) {
                swellHeight = Double.parseDouble(matcher.group(1).replace(',', '.'));
            }
        }

        return new QuerySignals(level, swellHeight, direction);
    }

    private String describeSpot(Spot spot) {
        String notes = spot.getNotes() != null && !spot.getNotes().isEmpty()
                ? String.join("; ", spot.getNotes())
                : "No additional notes.";
        return """
                Spot: %s
                Recommended level: %s
                Ideal swell: %s
                Ideal wind: %s
                Notes: %s
                """.formatted(
                spot.getName(),
                spot.getRecommendedLevel(),
                spot.getSwellBestDirection(),
                spot.getWindBestDirection(),
                notes
        );
    }
}
