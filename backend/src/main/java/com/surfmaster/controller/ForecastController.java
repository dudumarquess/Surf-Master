package com.surfmaster.controller;

import com.surfmaster.dto.ForecastDto;
import com.surfmaster.entities.Forecast;
import com.surfmaster.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/forecasts")
public class ForecastController {
    private final ForecastService forecastService;

    @GetMapping("/spot/{spotId}")
    public List<ForecastDto> getForecastsBySpot(@PathVariable Long spotId) {
        return forecastService.getLatestForSpot(spotId);
    }
}
