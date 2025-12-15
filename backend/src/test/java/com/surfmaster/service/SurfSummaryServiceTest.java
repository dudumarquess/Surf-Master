package com.surfmaster.service;

import com.surfmaster.dto.SurfSummaryDto;
import com.surfmaster.entities.ForecastWindow;
import com.surfmaster.entities.Spot;
import com.surfmaster.entities.SurfSummary;
import com.surfmaster.repository.SpotRepository;
import com.surfmaster.repository.SurfSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SurfSummaryServiceTest {

    @Mock
    private SurfSummaryRepository surfSummaryRepository;

    @Mock
    private SpotRepository spotRepository;

    private Clock clock;
    private SurfSummaryService surfSummaryService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2025-01-01T03:00:00Z"), ZoneOffset.UTC);
        surfSummaryService = new SurfSummaryService(surfSummaryRepository, spotRepository, clock);
    }

    @Test
    void returnsExistingSummaryWhenAvailable() {
        var spot = Spot.builder().id(4L).build();

        var summary = SurfSummary.builder()
                .id(9L)
                .spot(spot)
                .generatedAt(OffsetDateTime.now(clock).minusHours(1))
                .window(ForecastWindow.TODAY)
                .summary("cached summary")
                .score(7)
                .tags(List.of("cached"))
                .build();

        when(spotRepository.findById(4L)).thenReturn(Optional.of(spot));
        when(surfSummaryRepository.findLatestForSpotAndDay(eq(4L), anyString()))
                .thenReturn(summary);

        SurfSummaryDto dto = surfSummaryService.getOrGenerateToday(4L);

        assertThat(dto.summary()).isEqualTo("cached summary");
        verify(surfSummaryRepository, never()).save(any());

        // opcional: garante que o service NÃO está passando "TODAY"
        ArgumentCaptor<String> dayCaptor = ArgumentCaptor.forClass(String.class);
        verify(surfSummaryRepository).findLatestForSpotAndDay(eq(4L), dayCaptor.capture());
        assertThat(dayCaptor.getValue()).matches("\\d{4}-\\d{2}-\\d{2}");
    }

    @Test
    void generatesAndPersistsSummaryWhenMissing() {
        var spot = Spot.builder().id(1L).build();

        when(spotRepository.findById(1L)).thenReturn(Optional.of(spot));
        when(surfSummaryRepository.findLatestForSpotAndDay(eq(1L), anyString()))
                .thenReturn(null);

        when(surfSummaryRepository.save(any(SurfSummary.class))).thenAnswer(invocation -> {
            SurfSummary s = invocation.getArgument(0);
            s.setId(33L);
            return s;
        });

        SurfSummaryDto dto = surfSummaryService.getOrGenerateToday(1L);

        assertThat(dto.id()).isEqualTo(33L);
        assertThat(dto.summary()).contains("MVP summary");
        verify(surfSummaryRepository).save(any(SurfSummary.class));
    }
}
