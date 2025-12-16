package com.surfmaster.service;

import com.surfmaster.dto.RecommendationRequest;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.Objective;
import com.surfmaster.entities.Risk;
import com.surfmaster.entities.RiskType;
import com.surfmaster.entities.Spot;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.repository.SpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private ForecastService forecastService;

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void recommendRanksSpotsByScoreAndAppliesTopK() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusHours(6);

        Spot highScoreSpot = Spot.builder()
                .id(1L)
                .name("Prainha")
                .swellBestDirection(Direction.N)
                .windBestDirection(Direction.S)
                .build();
        Spot lowScoreSpot = Spot.builder()
                .id(2L)
                .name("Outra Praia")
                .swellBestDirection(Direction.N)
                .windBestDirection(Direction.S)
                .build();

        Forecast perfectWindow = Forecast.builder()
                .id(11L)
                .spot(highScoreSpot)
                .timestamp(start.plusHours(1))
                .swellHeight(1.0)
                .swellPeriod(12)
                .swellDirection(Direction.N)
                .windSpeed(8.0)
                .windDirection(Direction.S)
                .build();
        Forecast messyWindow = Forecast.builder()
                .id(12L)
                .spot(lowScoreSpot)
                .timestamp(start.plusHours(2))
                .swellHeight(3.0)
                .swellPeriod(10)
                .swellDirection(Direction.E)
                .windSpeed(25.0)
                .windDirection(Direction.N)
                .build();

        when(spotRepository.findAll()).thenReturn(List.of(highScoreSpot, lowScoreSpot));
        when(forecastService.getForecastsBetween(start, end)).thenReturn(List.of(perfectWindow, messyWindow));

        RecommendationRequest request = new RecommendationRequest(
                -43.0, -22.0, UserLevel.INTERMEDIATE, Objective.FUN,
                30.0, start, end, 1, 1
        );

        var response = recommendationService.recommend(request);

        assertThat(response.recommendations()).hasSize(1);
        assertThat(response.recommendations().get(0).spotId()).isEqualTo(highScoreSpot.getId());
        assertThat(response.recommendations().get(0).score()).isEqualTo(100.0);
        assertThat(response.recommendations().get(0).confidence()).isCloseTo(0.9, within(1e-9));
    }

    @Test
    void recommendBuildsRisksAndReasonsBasedOnForecastConditions() {
        OffsetDateTime start = OffsetDateTime.now();
        OffsetDateTime end = start.plusHours(4);

        Spot spot = Spot.builder()
                .id(5L)
                .name("Macumba")
                .swellBestDirection(Direction.S)
                .windBestDirection(Direction.E)
                .notes(List.of("Correnteza forte", "Fundo raso"))
                .build();

        Forecast adverseConditions = Forecast.builder()
                .id(21L)
                .spot(spot)
                .timestamp(start.plusHours(2))
                .swellHeight(2.5)
                .swellPeriod(9)
                .swellDirection(Direction.N)
                .windSpeed(22.0)
                .windDirection(Direction.W)
                .build();

        when(spotRepository.findAll()).thenReturn(List.of(spot));
        when(forecastService.getForecastsBetween(start, end)).thenReturn(List.of(adverseConditions));

        RecommendationRequest request = new RecommendationRequest(
                -43.0, -22.0, UserLevel.BEGINNER, Objective.FUN,
                40.0, start, end, 3, 2
        );

        var response = recommendationService.recommend(request);

        assertThat(response.recommendations()).hasSize(1);
        var item = response.recommendations().get(0);
        assertThat(item.score()).isEqualTo(0.0);
        assertThat(item.confidence()).isCloseTo(0.2, within(1e-9));
        assertThat(item.reasons()).hasSize(3);

        List<RiskType> riskTypes = item.risks().stream().map(Risk::type).toList();
        assertThat(riskTypes).contains(RiskType.STRONG_WIND, RiskType.TOO_BIG_FOR_LEVEL, RiskType.LOW_CONFIDENCE);
        assertThat(riskTypes.stream().filter(type -> type == RiskType.SPOT_NOTE)).hasSize(2);
    }
}
