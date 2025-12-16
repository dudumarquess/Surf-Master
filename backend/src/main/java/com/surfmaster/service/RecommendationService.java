package com.surfmaster.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.surfmaster.dto.RecommendationItem;
import com.surfmaster.dto.RecommendationRequest;
import com.surfmaster.dto.RecommendationResponse;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.Objective;
import com.surfmaster.entities.Reason;
import com.surfmaster.entities.ReasonType;
import com.surfmaster.entities.Risk;
import com.surfmaster.entities.RiskType;
import com.surfmaster.entities.Spot;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.repository.SpotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ForecastService forecastService;
    private final SpotRepository spotRepository;

    public RecommendationResponse recommend(RecommendationRequest request) {
        validateRequest(request);

        int topK = request.topK() != null ? request.topK() : 3;

        List<Forecast> forecasts = forecastService.getForecastsBetween(
                request.timeStart(),
                request.timeEnd()
        );

        Map<Long, List<Forecast>> forecastsBySpot = forecasts.stream()
                .collect(Collectors.groupingBy(f -> f.getSpot().getId()));

        List<RecommendationItem> items = new ArrayList<>();

        for (Spot spot : spotRepository.findAll()) {
            List<Forecast> spotForecasts = forecastsBySpot.get(spot.getId());
            if (spotForecasts == null || spotForecasts.isEmpty()) continue;

            // ordenar por timestamp
            spotForecasts = spotForecasts.stream()
                    .sorted(Comparator.comparing(Forecast::getTimestamp))
                    .toList();

            // V1: escolhe o melhor "ponto" (melhor hora) como janela de 1 ponto
            RecommendationItem best = buildBestItemForSpot(spot, spotForecasts, request);
            if (best != null) items.add(best);
        }

        List<RecommendationItem> top = items.stream()
                .sorted(Comparator.comparingDouble(i -> ((RecommendationItem) i).score() == null ? Double.NEGATIVE_INFINITY : ((RecommendationItem) i).score()).reversed())
                .limit(topK)
                .toList();

        return new RecommendationResponse(
                OffsetDateTime.now(),
                request.timeStart(),
                request.timeEnd(),
                top
        );
    }

    private RecommendationItem buildBestItemForSpot(Spot spot, List<Forecast> forecasts, RecommendationRequest req) {

        RecommendationItem bestItem = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Forecast f : forecasts) {
            double score = score(spot, f, req);

            if (score > bestScore) {
                bestScore = score;

                List<Reason> reasons = buildReasons(spot, f, req, score);
                List<Risk> risks = buildRisks(spot, f, req, score);

                bestItem = new RecommendationItem(
                        spot.getId(),
                        spot.getName(),
                        f.getTimestamp(),  // V1: janela = instante
                        f.getTimestamp(),
                        f.getTimestamp(),
                        score,
                        reasons,
                        risks,
                        confidenceFromScore(score)
                );
            }
        }

        return bestItem;
    }

    // --------- SCORE (V1 simples) ---------

    private double score(Spot spot, Forecast f, RecommendationRequest req) {
        // swell and wind direction: 0..1
        double swellDir = dirMatch(spot.getSwellBestDirection(), f.getSwellDirection());
        double windDir  = dirMatch(spot.getWindBestDirection(), f.getWindDirection());

        // swell altura: penaliza swell alto pra beginner
        double swellHeightScore = swellHeightScore(f.getSwellHeight(), req.userLevel());

        // penalidade de vento por objective
        double windPenalty = windPenalty(f.getWindSpeed(), req.objective());

        double windScore = windDir * (1.0 - windPenalty);
        double swellScore = swellDir * swellHeightScore;

        // pesos
        double score01 = 0.55 * swellScore + 0.45 * windScore;

        return 100.0 * clamp01(score01);
    }

    private double swellHeightScore(Double h, UserLevel level) {
        if (h == null || h <= 0) return 0.0;

        double maxOk = switch (level) {
            case BEGINNER -> 1.2;
            case INTERMEDIATE -> 2.0;
            case ADVANCED -> 3.0;
        };

        if (h <= maxOk) return 1.0;
        double worst = maxOk * 2.0;
        if (h >= worst) return 0.0;
        return 1.0 - (h - maxOk) / (worst - maxOk);
    }

    private double windPenalty(Double windSpeed, Objective objective) {
        if (windSpeed == null) return 0.5; // conservador

        // NOTE: adjust according to unit (km/h vs m/s)
        double OK = 12.0;
        double BAD = 25.0;

        double base;
        if (windSpeed <= OK) base = 0.0;
        else if (windSpeed >= BAD) base = 1.0;
        else base = (windSpeed - OK) / (BAD - OK);

        double mult = switch (objective) {
            case FUN -> 1.25;
            case TRAINING -> 0.85;
        };

        return clamp01(base * mult);
    }

    private double dirMatch(Direction best, Direction actual) {
        if (best == null || actual == null) return 0.0;
        return best == actual ? 1.0 : 0.0; // V1 ultra simples
    }

    private double confidenceFromScore(double score) {
        // score 0..100 -> confidence 0.2..0.9
        double c = 0.2 + 0.7 * (score / 100.0);
        return clamp01(c);
    }

    private double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }

    // --------- REASONS / RISKS ---------

    private List<Reason> buildReasons(Spot spot, Forecast f, RecommendationRequest req, double score) {
        List<Reason> reasons = new ArrayList<>();

        if (spot.getSwellBestDirection() == f.getSwellDirection()) {
            reasons.add(new Reason(ReasonType.SWELL, "Swell aligned with the spot's ideal direction"));
        } else {
            reasons.add(new Reason(ReasonType.SWELL, "Swell acceptable (not perfect but within range)"));
        }

        if (spot.getWindBestDirection() == f.getWindDirection()) {
            reasons.add(new Reason(ReasonType.WIND, "Wind aligned with the spot's ideal direction"));
        } else {
            reasons.add(new Reason(ReasonType.WIND, "Wind not ideal, but penalty is under control"));
        }

        reasons.add(new Reason(ReasonType.OTHER, String.format("Score V1: %.1f/100", score)));

        return reasons;
    }

    private List<Risk> buildRisks(Spot spot, Forecast f, RecommendationRequest req, double score) {
        List<Risk> risks = new ArrayList<>();

        if (f.getWindSpeed() != null && f.getWindSpeed() > 18) {
            risks.add(new Risk(RiskType.STRONG_WIND, "Moderate/strong wind may ruin wave formation"));
        }

        if (req.userLevel() == UserLevel.BEGINNER && f.getSwellHeight() != null && f.getSwellHeight() > 1.2) {
            risks.add(new Risk(RiskType.TOO_BIG_FOR_LEVEL, "Swell height may be above the ideal range for beginners"));
        }

        if (score < 60) {
            risks.add(new Risk(RiskType.LOW_CONFIDENCE, "Inconsistent conditions: recommendation has low confidence"));
        }

        // notas do spot como alertas
        if (spot.getNotes() != null) {
            for (String note : spot.getNotes().stream().limit(2).toList()) {
                risks.add(new Risk(RiskType.SPOT_NOTE, note));
            }
        }

        return risks;
    }


    private void validateRequest(RecommendationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.latitude() == null || request.longitude() == null) {
            throw new IllegalArgumentException("Latitude and Longitude cannot be null");
        }

        if (request.latitude() < -90 || request.latitude() > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }

        if (request.longitude() < -180 || request.longitude() > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (request.userLevel() == null) {
            throw new IllegalArgumentException("User level cannot be null");
        }

        if (request.objective() == null) {
            throw new IllegalArgumentException("Objective cannot be null");
        }

        if (request.maxDistanceKm() == null || request.maxDistanceKm() <= 0 || request.maxDistanceKm() > 200.0) {
            throw new IllegalArgumentException("Max distance must be positive and less than or equal to 200 km");
        }

        if (request.timeStart() == null || request.timeEnd() == null || request.timeEnd().isBefore(request.timeStart())) {
            throw new IllegalArgumentException("Invalid time range");
        }
    }
}
