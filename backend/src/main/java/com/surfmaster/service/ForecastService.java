package com.surfmaster.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.surfmaster.config.ForecastProperties;
import com.surfmaster.dto.ForecastDto;
import com.surfmaster.dto.ForecastSyncResponse;
import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.Spot;
import com.surfmaster.forecast.providers.ForecastProvider;
import com.surfmaster.forecast.providers.ForecastProviderRegistry;
import com.surfmaster.mappers.EntityMapper;
import com.surfmaster.repository.ForecastRepository;
import com.surfmaster.repository.SpotRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForecastService {
    private final ForecastRepository forecastRepository;
    private final SpotRepository spotRepository;
    private final ForecastProviderRegistry forecastProviderRegistry;
    private final ForecastProperties forecastProperties;

    private final Map<Long, OffsetDateTime> lastFetchBySpot = new ConcurrentHashMap<>();

    @Transactional
    public List<ForecastDto> getForecastsForSpot(Long spotId, OffsetDateTime from, OffsetDateTime to) {
        spotRepository.findById(spotId)
                .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId));

        OffsetDateTime effectiveFrom = from != null ? from : defaultFromWindow();

        return forecastRepository.findBySpotIdAfter(spotId, effectiveFrom).stream()
                .filter(f -> to == null || !f.getTimestamp().isAfter(to))
                .map(EntityMapper::toDto)
                .toList();
    }

    @Transactional
    public List<Forecast> getForecastsBetween(
            OffsetDateTime from,
            OffsetDateTime to
    ) {
        return forecastRepository.findAllInRangeWithSpot(from, to);
    }

    @Transactional
    public ForecastSyncResponse syncWithProvider(Long spotId, OffsetDateTime from, OffsetDateTime to, boolean force) {
        ForecastProvider provider = forecastProviderRegistry.getConfiguredProvider()
                .orElseThrow(() -> new IllegalStateException("No external forecast provider configured. Set surf.forecast.provider=stormglass to enable external sync."));

        List<Spot> targets = spotId != null
                ? List.of(spotRepository.findById(spotId)
                        .orElseThrow(() -> new IllegalArgumentException("Spot not found: " + spotId)))
                : spotRepository.findAll();

        if (targets.isEmpty()) {
            return new ForecastSyncResponse(0, 0, List.of());
        }

        List<Forecast> persisted = new ArrayList<>();
        for (Spot spot : targets) {
            persisted.addAll(refreshSpotForecasts(provider, spot, from, to, force));
        }

        return new ForecastSyncResponse(
                targets.size(),
                persisted.size(),
                persisted.stream().map(EntityMapper::toDto).toList()
        );
    }

    private List<Forecast> refreshSpotForecasts(ForecastProvider provider, Spot spot, OffsetDateTime from, OffsetDateTime to, boolean force) {
        if (!provider.supports(spot)) {
            log.debug("Forecast provider {} does not support spot {}", provider.getSource(), spot.getId());
            return List.of();
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime lastFetch = lastFetchBySpot.get(spot.getId());
        if (!force && lastFetch != null && lastFetch.isAfter(now.minus(forecastProperties.ttl()))) {
            log.debug("Skipping spot {} because last fetch via {} happened at {}", spot.getId(), provider.getSource(), lastFetch);
            return List.of();
        }

        OffsetDateTime targetFrom = from != null ? from : now;
        OffsetDateTime targetTo = determineTargetTo(targetFrom, to);

        List<Forecast> forecasts = provider.fetch(spot, targetFrom, targetTo);
        if (forecasts.isEmpty()) {
            log.debug("Provider {} returned no forecasts for spot {}", provider.getSource(), spot.getName());
            return List.of();
        }

        forecastRepository.deleteAllForSpotBetween(spot.getId(), targetFrom, targetTo);
        List<Forecast> saved = forecastRepository.saveAll(forecasts);
        lastFetchBySpot.put(spot.getId(), now);

        return saved;
    }

    private OffsetDateTime determineTargetTo(OffsetDateTime from, OffsetDateTime requestedTo) {
        if (requestedTo == null) {
            return from.plusHours(forecastProperties.getStormglass().getHorizonHours());
        }
        if (requestedTo.isBefore(from)) {
            return from;
        }
        return requestedTo;
    }

    private OffsetDateTime defaultFromWindow() {
        int hours = forecastProperties.getStormglass().getHorizonHours();
        if (hours <= 0) {
            hours = 72;
        }
        return OffsetDateTime.now().minusHours(hours);
    }
}
