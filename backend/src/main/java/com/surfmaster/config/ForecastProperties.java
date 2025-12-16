package com.surfmaster.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "surf.forecast")
public class ForecastProperties {

    private String provider = "windguru";

    @Min(1)
    private int ttlMinutes = 60;

    private Stormglass stormglass = new Stormglass();

    public Duration ttl() {
        return Duration.ofMinutes(ttlMinutes);
    }

    @Getter
    @Setter
    public static class Stormglass {
        private String baseUrl = "https://api.stormglass.io/v2";
        private String endpoint = "/weather/point";
        private String apiKey;
        private String source = "noaa";

        @Min(1)
        private int horizonHours = 24;

        private List<String> params = new ArrayList<>(List.of(
                "swellHeight",
                "swellDirection",
                "swellPeriod",
                "windSpeed",
                "windDirection",
                "waterTemperature"
        ));
    }
}
