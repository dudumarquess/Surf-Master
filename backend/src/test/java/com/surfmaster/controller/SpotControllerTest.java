package com.surfmaster.controller;

import com.surfmaster.dto.SpotDto;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.service.SpotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpotController.class)
class SpotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpotService spotService;

    @Test
    void listSpotsReturnsDtoList() throws Exception {
        var dto = new SpotDto(
                1L, "Joaquina", -48.5, -27.6,
                Direction.S, Direction.N, UserLevel.INTERMEDIATE,
                List.of("Melhor de sul")
        );
        when(spotService.listAll()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/spots"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Joaquina"));
    }

    @Test
    void getSpotByIdReturnsDto() throws Exception {
        var dto = new SpotDto(
                8L, "Matadeiro", -48.55, -27.72,
                Direction.SW, Direction.NE, UserLevel.BEGINNER,
                List.of()
        );
        when(spotService.getById(8L)).thenReturn(dto);

        mockMvc.perform(get("/api/spots/{id}", 8L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.recommendedLevel").value("BEGINNER"));
    }
}
