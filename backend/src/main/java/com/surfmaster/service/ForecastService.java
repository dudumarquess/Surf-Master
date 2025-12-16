package com.surfmaster.service;

import com.surfmaster.dto.ForecastDto;
import com.surfmaster.entities.Forecast;
import com.surfmaster.mappers.EntityMapper;
import com.surfmaster.repository.ForecastRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForecastService {
    private final ForecastRepository forecastRepository;

    @Transactional
    public List<ForecastDto> getLatestForSpot(Long spotId) {
        OffsetDateTime now = OffsetDateTime.now();
        return forecastRepository.findBySpotIdAfter(spotId, now).stream()
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

}
