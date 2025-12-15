package com.surfmaster.service;

import com.surfmaster.dto.SpotDto;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.Spot;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.repository.SpotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotServiceTest {

    @Mock
    private SpotRepository spotRepository;

    @InjectMocks
    private SpotService spotService;

    @Test
    void listAllMapsEntitiesToDtos() {
        var spot = Spot.builder()
                .id(1L)
                .name("Praia Mole")
                .longitude(-48.4)
                .latitude(-27.6)
                .swellBestDirection(Direction.SE)
                .windBestDirection(Direction.NW)
                .recommendedLevel(UserLevel.INTERMEDIATE)
                .notes(List.of("inverno"))
                .build();
        when(spotRepository.findAll()).thenReturn(List.of(spot));

        List<SpotDto> result = spotService.listAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Praia Mole");
        assertThat(result.get(0).recommendedLevel()).isEqualTo(UserLevel.INTERMEDIATE);
    }

    @Test
    void getByIdReturnsMappedEntity() {
        var spot = Spot.builder().id(3L).name("Campeche").build();
        when(spotRepository.findById(3L)).thenReturn(Optional.of(spot));

        SpotDto dto = spotService.getById(3L);

        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.name()).isEqualTo("Campeche");
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(spotRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> spotService.getById(99L));
    }
}
