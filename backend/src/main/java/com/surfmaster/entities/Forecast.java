package com.surfmaster.entities;

import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Spot;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.OffsetDateTime;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Forecast {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Spot spot;

    private OffsetDateTime timestamp;

    private Double swellHeight;

    private int swellPeriod;

    @Enumerated(EnumType.STRING)
    private Direction swellDirection;

    private Double windSpeed;

    @Enumerated(EnumType.STRING)
    private Direction windDirection;

    private Double tideHeight;

    private int waterTemperature;

    @Enumerated(EnumType.STRING)
    private ForecastSource dataSource;

    private OffsetDateTime validFrom;

    private OffsetDateTime validTo;

}
