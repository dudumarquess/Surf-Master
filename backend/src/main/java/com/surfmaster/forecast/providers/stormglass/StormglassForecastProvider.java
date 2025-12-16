package com.surfmaster.forecast.providers.stormglass;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.surfmaster.config.ForecastProperties;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.ForecastSource;
import com.surfmaster.entities.Spot;
import com.surfmaster.forecast.providers.ForecastProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class StormglassForecastProvider implements ForecastProvider {

    private static final Direction[] DIRECTIONS = Direction.values();

    private final ForecastProperties forecastProperties;
    private final WebClient client;

    public StormglassForecastProvider(WebClient.Builder webClientBuilder, ForecastProperties forecastProperties) {
        this.forecastProperties = forecastProperties;
        this.client = webClientBuilder
                .baseUrl(forecastProperties.getStormglass().getBaseUrl())
                .build();
    }

    @Override
    public ForecastSource getSource() {
        return ForecastSource.STORMGLASS;
    }

    @Override
    public boolean supports(Spot spot) {
        return spot != null && spot.getLatitude() != null && spot.getLongitude() != null;
    }

    @Override
    public List<Forecast> fetch(Spot spot, OffsetDateTime from, OffsetDateTime to) {
        if (!supports(spot)) {
            return List.of();
        }

        OffsetDateTime effectiveFrom = from != null ? from : OffsetDateTime.now();
        OffsetDateTime effectiveTo = normalizeEnd(effectiveFrom, to);

        StormglassResponse response = requestForecast(spot, effectiveFrom, effectiveTo);
        if (response == null || response.hours() == null) {
            return List.of();
        }

        return response.hours().stream()
                .map(hour -> toForecast(spot, hour))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Forecast::getTimestamp))
                .toList();
    }

    private OffsetDateTime normalizeEnd(OffsetDateTime from, OffsetDateTime requestedEnd) {
        if (requestedEnd == null) {
            return from.plusHours(forecastProperties.getStormglass().getHorizonHours());
        }
        if (requestedEnd.isBefore(from)) {
            return from;
        }
        return requestedEnd;
    }

    private StormglassResponse requestForecast(Spot spot, OffsetDateTime from, OffsetDateTime to) {
        ForecastProperties.Stormglass config = forecastProperties.getStormglass();
        String apiKey = config.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Stormglass API key is not configured (surf.forecast.stormglass.api-key)");
        }

        String params = String.join(",", config.getParams());

        return client.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(config.getEndpoint())
                            .queryParam("lat", spot.getLatitude())
                            .queryParam("lng", spot.getLongitude())
                            .queryParam("params", params)
                            .queryParam("start", from.toEpochSecond())
                            .queryParam("end", to.toEpochSecond());
                    if (config.getSource() != null && !config.getSource().isBlank()) {
                        uriBuilder.queryParam("source", config.getSource());
                    }
                    return uriBuilder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, apiKey.trim())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(body -> {
                            log.warn("Stormglass API error {} for spot {}: {}", response.statusCode(), spot.getName(), body);
                            return Mono.error(new IllegalStateException("Stormglass API error: " + response.statusCode()));
                        }))
                .bodyToMono(StormglassResponse.class)
                .block(Duration.ofSeconds(30));
    }

    private Forecast toForecast(Spot spot, StormglassResponse.Hour hour) {
        OffsetDateTime timestamp = hour.time();
        if (timestamp == null) {
            return null;
        }

        Double swellHeight = resolveValue(hour.swellHeight());
        Double swellPeriod = resolveValue(hour.swellPeriod());
        Double swellDirection = resolveValue(hour.swellDirection());
        Double windSpeed = resolveValue(hour.windSpeed());
        Double windDirection = resolveValue(hour.windDirection());
        Double tideHeight = resolveValue(hour.tideHeight());
        Double waterTemperature = resolveValue(hour.waterTemperature());

        return Forecast.builder()
                .spot(spot)
                .timestamp(timestamp)
                .swellHeight(swellHeight)
                .swellPeriod(swellPeriod != null ? swellPeriod.intValue() : 0)
                .swellDirection(toDirection(swellDirection))
                .windSpeed(windSpeed)
                .windDirection(toDirection(windDirection))
                .tideHeight(tideHeight)
                .waterTemperature(waterTemperature != null ? (int) Math.round(waterTemperature) : 0)
                .dataSource(ForecastSource.STORMGLASS)
                .build();
    }

    private Direction toDirection(Double degrees) {
        if (degrees == null || DIRECTIONS.length == 0) {
            return null;
        }
        double normalized = (degrees % 360 + 360) % 360;
        int index = (int) Math.round(normalized / 45.0) % DIRECTIONS.length;
        return DIRECTIONS[index];
    }

    private Double resolveValue(Map<String, Double> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        String preferredSource = forecastProperties.getStormglass().getSource();
        if (preferredSource != null) {
            Double v = values.get(preferredSource);
            if (v != null) {
                return v;
            }
        }
        return values.values().stream()
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }
}
