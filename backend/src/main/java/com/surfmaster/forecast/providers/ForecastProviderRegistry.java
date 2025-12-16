package com.surfmaster.forecast.providers;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.surfmaster.config.ForecastProperties;
import com.surfmaster.entities.ForecastSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForecastProviderRegistry {

    private final List<ForecastProvider> providers;
    private final ForecastProperties forecastProperties;

    public Optional<ForecastProvider> getConfiguredProvider() {
        ForecastSource desiredSource = resolveConfiguredSource();
        if (desiredSource == null) {
            return Optional.empty();
        }
        return providers.stream()
                .filter(provider -> provider.getSource() == desiredSource)
                .findFirst();
    }

    private ForecastSource resolveConfiguredSource() {
        String providerName = forecastProperties.getProvider();
        if (providerName == null || providerName.isBlank()) {
            return null;
        }
        try {
            return ForecastSource.valueOf(providerName.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warn("Unsupported forecast provider configured: {}", providerName);
            return null;
        }
    }
}
