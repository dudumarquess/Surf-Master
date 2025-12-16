package com.surfmaster.forecast.providers;

import java.time.OffsetDateTime;
import java.util.List;

import com.surfmaster.entities.Forecast;
import com.surfmaster.entities.ForecastSource;
import com.surfmaster.entities.Spot;

public interface ForecastProvider {
    ForecastSource getSource();
    boolean supports(Spot spot);

    List<Forecast> fetch(Spot spot, OffsetDateTime from, OffsetDateTime to);
}
