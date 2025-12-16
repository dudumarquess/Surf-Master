package com.surfmaster.service;

import com.surfmaster.config.ForecastProperties;
import com.surfmaster.dto.ForecastDto;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.ForecastSource;
import com.surfmaster.entities.Spot;
import com.surfmaster.repository.ForecastRepository;
import com.surfmaster.repository.SpotRepository;
import com.surfmaster.forecast.providers.ForecastProviderRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ForecastServiceTest {

    @Mock
    private ForecastRepository forecastRepository;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private ForecastProviderRegistry forecastProviderRegistry;

    @Mock
    private ForecastProperties forecastProperties;

    @InjectMocks
    private ForecastService forecastService;

    @Test
    void getForecastsForSpotUsesRepositoryAndMapsResult() {
        var spot = Spot.builder().id(7L).build();
        when(spotRepository.findById(7L)).thenReturn(Optional.of(spot));
        var stormglass = new ForecastProperties.Stormglass();
        stormglass.setHorizonHours(72);
        when(forecastProperties.getStormglass()).thenReturn(stormglass);
        var now = OffsetDateTime.now();
        var forecast = Forecast.builder()
                .id(11L)
                .spot(spot)
                .timestamp(now)
                .swellHeight(1.8)
                .swellPeriod(14)
                .swellDirection(Direction.SE)
                .windSpeed(5.0)
                .windDirection(Direction.NW)
                .tideHeight(0.9)
                .waterTemperature(23)
                .dataSource(ForecastSource.MAGICSEAWEED)
                .build();
        when(forecastRepository.findBySpotIdAfter(eq(7L), any(OffsetDateTime.class)))
                .thenReturn(List.of(forecast));

        List<ForecastDto> result = forecastService.getForecastsForSpot(7L, null, null);

        assertThat(result).hasSize(1);
        ForecastDto dto = result.get(0);
        assertThat(dto.id()).isEqualTo(11L);
        assertThat(dto.spotId()).isEqualTo(7L);
        assertThat(dto.swellHeight()).isEqualTo(1.8);

        ArgumentCaptor<OffsetDateTime> captor = ArgumentCaptor.forClass(OffsetDateTime.class);
        verify(forecastRepository).findBySpotIdAfter(eq(7L), captor.capture());
        assertThat(captor.getValue()).isNotNull();
    }
}
