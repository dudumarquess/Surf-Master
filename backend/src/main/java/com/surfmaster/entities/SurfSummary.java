package com.surfmaster.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class SurfSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Spot spot;

    private OffsetDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "forecast_window")
    private ForecastWindow window;

    private String summary;

    private int score;

    private List<String> tags;
}
