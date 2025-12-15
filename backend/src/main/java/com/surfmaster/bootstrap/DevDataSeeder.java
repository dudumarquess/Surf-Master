// src/main/java/com/surfmaster/bootstrap/DevDataSeeder.java
package com.surfmaster.bootstrap;

import com.surfmaster.entities.*;
import com.surfmaster.repository.SpotRepository;
import com.surfmaster.repository.ForecastRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final SpotRepository spotRepo;
    private final ForecastRepository forecastRepo;

    @Override
    public void run(String... args) {
        if (spotRepo.count() > 0) return; // já populado

        Spot ericeira = Spot.builder()
                .name("Ericeira - Ribeira d’Ilhas")
                .latitude(38.9931).longitude(-9.4146)
                .swellBestDirection(Direction.NW)
                .windBestDirection(Direction.E)
                .recommendedLevel(UserLevel.INTERMEDIATE)
                .notes(List.of("Direita longa", "Funciona melhor com maré média"))
                .build();

        Spot carcavelos = Spot.builder()
                .name("Carcavelos")
                .latitude(38.6789).longitude(-9.3210)
                .swellBestDirection(Direction.W)
                .windBestDirection(Direction.E)
                .recommendedLevel(UserLevel.BEGGINER)
                .notes(List.of("Beach break", "Melhor com offshore de leste"))
                .build();

        spotRepo.saveAll(List.of(ericeira, carcavelos));

        // Forecasts de exemplo (próximas horas)
        var now = OffsetDateTime.now().withMinute(0).withSecond(0).withNano(0);

        forecastRepo.saveAll(List.of(
                // Ericeira
                com.surfmaster.entities.Forecast.builder()
                        .spot(ericeira)
                        .timestamp(now.plusHours(1))
                        .swellHeight(1.4).swellPeriod(11).swellDirection(Direction.NW)
                        .windSpeed(6.0).windDirection(Direction.E)
                        .tideHeight(1.2).waterTemperature(17)
                        .dataSource(ForecastSource.SEED)
                        .validFrom(now).validTo(now.plusHours(3))
                        .build(),
                com.surfmaster.entities.Forecast.builder()
                        .spot(ericeira)
                        .timestamp(now.plusHours(3))
                        .swellHeight(1.6).swellPeriod(12).swellDirection(Direction.NW)
                        .windSpeed(5.0).windDirection(Direction.E)
                        .tideHeight(1.0).waterTemperature(17)
                        .dataSource(ForecastSource.SEED)
                        .validFrom(now.plusHours(2)).validTo(now.plusHours(5))
                        .build(),

                // Carcavelos
                com.surfmaster.entities.Forecast.builder()
                        .spot(carcavelos)
                        .timestamp(now.plusHours(1))
                        .swellHeight(1.2).swellPeriod(10).swellDirection(Direction.W)
                        .windSpeed(8.0).windDirection(Direction.E)
                        .tideHeight(1.1).waterTemperature(18)
                        .dataSource(ForecastSource.SEED)
                        .validFrom(now).validTo(now.plusHours(3))
                        .build()
        ));

        System.out.println("✔ Seed dev: " + spotRepo.count() + " spots e "
                + forecastRepo.count() + " forecasts.");
    }
}
