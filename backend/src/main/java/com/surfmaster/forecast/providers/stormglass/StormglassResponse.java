package com.surfmaster.forecast.providers.stormglass;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StormglassResponse(
        List<Hour> hours
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Hour(
            OffsetDateTime time,
            @JsonProperty("swellHeight") Map<String, Double> swellHeight,
            @JsonProperty("swellDirection") Map<String, Double> swellDirection,
            @JsonProperty("swellPeriod") Map<String, Double> swellPeriod,
            @JsonProperty("windSpeed") Map<String, Double> windSpeed,
            @JsonProperty("windDirection") Map<String, Double> windDirection,
            @JsonProperty("tideHeight") Map<String, Double> tideHeight,
            @JsonProperty("waterTemperature") Map<String, Double> waterTemperature
    ) {}
}
