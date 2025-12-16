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
                .recommendedLevel(UserLevel.BEGINNER)
                .notes(List.of("Beach break", "Melhor com offshore de leste"))
                .build();

        Spot peniche = Spot.builder()
                .name("Peniche - Supertubos")
                .latitude(39.3529).longitude(-9.3806)
                .swellBestDirection(Direction.W)
                .windBestDirection(Direction.E)
                .recommendedLevel(UserLevel.ADVANCED)
                .notes(List.of("Tubos pesados", "Funciona bem com swell longo"))
                .build();

        Spot nazare = Spot.builder()
                .name("Nazaré - Praia do Norte")
                .latitude(39.6020).longitude(-9.0700)
                .swellBestDirection(Direction.NW)
                .windBestDirection(Direction.E)
                .recommendedLevel(UserLevel.ADVANCED)
                .notes(List.of("Ondas gigantes no inverno", "Atenção às correntes"))
                .build();

        Spot caparica = Spot.builder()
                .name("Costa da Caparica - Praia da Rainha")
                .latitude(38.6369).longitude(-9.2326)
                .swellBestDirection(Direction.SW)
                .windBestDirection(Direction.NE)
                .recommendedLevel(UserLevel.INTERMEDIATE)
                .notes(List.of("Funciona com maré média a vazante", "Pier ajuda a alinhar"))
                .build();

        Spot matosinhos = Spot.builder()
                .name("Matosinhos")
                .latitude(41.1855).longitude(-8.7154)
                .swellBestDirection(Direction.NW)
                .windBestDirection(Direction.E)
                .recommendedLevel(UserLevel.BEGINNER)
                .notes(List.of("Ondas suaves", "Boa opção para aulas"))
                .build();

        spotRepo.saveAll(List.of(ericeira, carcavelos, peniche, nazare, caparica, matosinhos));

        // Forecasts de exemplo (próximas horas)
        var now = OffsetDateTime.now().withMinute(0).withSecond(0).withNano(0);

        forecastRepo.saveAll(List.of(
                // Ericeira
                Forecast.builder()
                        .spot(ericeira)
                        .timestamp(now.plusHours(1))
                        .swellHeight(1.4).swellPeriod(11).swellDirection(Direction.NW)
                        .windSpeed(6.0).windDirection(Direction.E)
                        .tideHeight(1.2).waterTemperature(17)
                        .dataSource(ForecastSource.SEED)
                        .build(),
                Forecast.builder()
                        .spot(ericeira)
                        .timestamp(now.plusHours(3))
                        .swellHeight(1.6).swellPeriod(12).swellDirection(Direction.NW)
                        .windSpeed(5.0).windDirection(Direction.E)
                        .tideHeight(1.0).waterTemperature(17)
                        .dataSource(ForecastSource.SEED)
                        .build(),

                // Carcavelos
                Forecast.builder()
                        .spot(carcavelos)
                        .timestamp(now.plusHours(1))
                        .swellHeight(1.2).swellPeriod(10).swellDirection(Direction.W)
                        .windSpeed(8.0).windDirection(Direction.E)
                        .tideHeight(1.1).waterTemperature(18)
                        .dataSource(ForecastSource.SEED)
                        .build(),
                Forecast.builder()
                        .spot(carcavelos)
                        .timestamp(now.plusHours(4))
                        .swellHeight(1.0).swellPeriod(9).swellDirection(Direction.W)
                        .windSpeed(12.0).windDirection(Direction.SE)
                        .tideHeight(0.8).waterTemperature(18)
                        .dataSource(ForecastSource.SEED)
                        .build(),

                // Peniche
                Forecast.builder()
                        .spot(peniche)
                        .timestamp(now.plusHours(2))
                        .swellHeight(2.0).swellPeriod(14).swellDirection(Direction.W)
                        .windSpeed(7.0).windDirection(Direction.E)
                        .tideHeight(1.4).waterTemperature(16)
                        .dataSource(ForecastSource.SEED)
                        .build(),
                Forecast.builder()
                        .spot(peniche)
                        .timestamp(now.plusHours(6))
                        .swellHeight(2.5).swellPeriod(15).swellDirection(Direction.NW)
                        .windSpeed(9.0).windDirection(Direction.E)
                        .tideHeight(1.1).waterTemperature(16)
                        .dataSource(ForecastSource.SEED)
                        .build(),

                // Nazaré
                Forecast.builder()
                        .spot(nazare)
                        .timestamp(now.plusHours(3))
                        .swellHeight(3.5).swellPeriod(16).swellDirection(Direction.NW)
                        .windSpeed(10.0).windDirection(Direction.E)
                        .tideHeight(1.3).waterTemperature(17)
                        .dataSource(ForecastSource.SEED)
                        .build(),
                Forecast.builder()
                        .spot(nazare)
                        .timestamp(now.plusHours(7))
                        .swellHeight(4.0).swellPeriod(17).swellDirection(Direction.NW)
                        .windSpeed(14.0).windDirection(Direction.SE)
                        .tideHeight(0.9).waterTemperature(16)
                        .dataSource(ForecastSource.SEED)
                        .build(),

                // Caparica
                Forecast.builder()
                        .spot(caparica)
                        .timestamp(now.plusHours(2))
                        .swellHeight(1.1).swellPeriod(9).swellDirection(Direction.SW)
                        .windSpeed(5.0).windDirection(Direction.NE)
                        .tideHeight(1.0).waterTemperature(19)
                        .dataSource(ForecastSource.SEED)
                        .build(),
                Forecast.builder()
                        .spot(caparica)
                        .timestamp(now.plusHours(5))
                        .swellHeight(1.4).swellPeriod(10).swellDirection(Direction.SW)
                        .windSpeed(9.0).windDirection(Direction.NE)
                        .tideHeight(0.7).waterTemperature(19)
                        .dataSource(ForecastSource.SEED)
                        .build(),

                // Matosinhos
                Forecast.builder()
                        .spot(matosinhos)
                        .timestamp(now.plusHours(1))
                        .swellHeight(0.9).swellPeriod(8).swellDirection(Direction.NW)
                        .windSpeed(4.0).windDirection(Direction.E)
                        .tideHeight(1.5).waterTemperature(16)
                        .dataSource(ForecastSource.SEED)
                        .build(),
                Forecast.builder()
                        .spot(matosinhos)
                        .timestamp(now.plusHours(4))
                        .swellHeight(1.1).swellPeriod(9).swellDirection(Direction.NW)
                        .windSpeed(6.0).windDirection(Direction.SE)
                        .tideHeight(1.2).waterTemperature(16)
                        .dataSource(ForecastSource.SEED)
                        .build()
        ));

        System.out.println("✔ Seed dev: " + spotRepo.count() + " spots e "
                + forecastRepo.count() + " forecasts.");
    }
}
