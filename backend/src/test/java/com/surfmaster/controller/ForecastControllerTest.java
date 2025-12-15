package com.surfmaster.controller;

import com.surfmaster.dto.ForecastDto;
import com.surfmaster.entities.Direction;
import com.surfmaster.entities.ForecastSource;
import com.surfmaster.service.ForecastService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForecastController.class)
class ForecastControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ForecastService forecastService;

    @Test
    void getForecastsForSpotReturnsList() throws Exception {
        var now = OffsetDateTime.now();
        var dto = new ForecastDto(
                1L, 10L, now,
                2.3, 14, Direction.N,
                5.0, Direction.S,
                1.2, 21, ForecastSource.MAGICSEAWEED,
                now, now.plusHours(3)
        );
        when(forecastService.getLatestForSpot(10L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/forecasts/spot/{spotId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].swellHeight").value(2.3));
    }
}
