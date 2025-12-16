package com.surfmaster.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.surfmaster.dto.RecommendationRequest;
import com.surfmaster.dto.RecommendationResponse;
import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.Spot;
import com.surfmaster.repository.SpotRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    
    private final ForecastService forecastService;
    private final SpotRepository spotRepository;

    public List<RecommendationResponse> recommend(RecommendationRequest request) {
        validateRequest(request);

        List<Forecast> forecasts = forecastService.getForecastsBetween(
            request.timeStart(),
            request.timeEnd()
        );

        Map<Long, List<Forecast>> forecastsBySpot = forecasts.stream()
            .collect(Collectors.groupingBy(f -> f.getSpot().getId()));

        List<RecommendationResponse> recommendations = new ArrayList<>();

        for (Spot spot : spotRepository.findAll()) {
            List<Forecast> spotForecasts = forecastsBySpot.get(spot.getId());
            if (spotForecasts == null || spotForecasts.isEmpty()) {
                continue;
            }
        }

        return recommendations.stream()
            .sorted(Comparator.comparingDouble(RecommendationResponse::score).reversed())
            .limit(request.topK())
            .toList();

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
