package com.surfmaster.entities;

import jakarta.persistence.*;

import java.util.List;

import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Double longitude;

    private Double latitude;

    @Enumerated(EnumType.STRING)
    private Direction swellBestDirection;

    @Enumerated(EnumType.STRING)
    private Direction windBestDirection;

    @Enumerated(EnumType.STRING)
    private UserLevel recommendedLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "spot_notes", joinColumns = @JoinColumn(name = "spot_id"))
    @Column(name = "note")
    private List<String> notes;
}
