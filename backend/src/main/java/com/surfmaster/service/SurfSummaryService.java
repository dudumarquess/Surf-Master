package com.surfmaster.service;

import com.surfmaster.dto.SurfSummaryDto;
import com.surfmaster.entities.ForecastWindow;
import com.surfmaster.entities.SurfSummary;
import com.surfmaster.mappers.EntityMapper;
import com.surfmaster.repository.SpotRepository;
import com.surfmaster.repository.SurfSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurfSummaryService {
    private final SurfSummaryRepository surfSummaryRepository;
    private final SpotRepository spotRepository;
    private final Clock clock;

    public SurfSummaryDto getOrGenerateToday(Long spotId) {
        var spot = spotRepository.findById(spotId).orElseThrow();
        var today = LocalDate.now(clock);

        var existing = surfSummaryRepository.findLatestForSpotAndDay(spotId, today.toString());
        if (existing != null) return EntityMapper.toDto(existing);

        // generate stub (replace with LLM later)
        var s = SurfSummary.builder()
                .spot(spot)
                .generatedAt(OffsetDateTime.now(clock))
                .window(ForecastWindow.TODAY)
                .summary("MVP summary: check swell/period and wind for best window")
                .score(6)
                .tags(List.of("stub"))
                .build();
        return EntityMapper.toDto(surfSummaryRepository.save(s));
    }
}
