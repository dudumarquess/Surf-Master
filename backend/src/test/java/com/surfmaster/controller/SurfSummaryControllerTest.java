package com.surfmaster.controller;

import com.surfmaster.dto.SurfSummaryDto;
import com.surfmaster.entities.ForecastWindow;
import com.surfmaster.service.SurfSummaryService;
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

@WebMvcTest(SurfSummaryController.class)
class SurfSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SurfSummaryService surfSummaryService;

    @Test
    void getTodaySummaryReturnsServiceData() throws Exception {
        var dto = new SurfSummaryDto(
                4L, 3L, OffsetDateTime.now(), ForecastWindow.TODAY,
                "Boas ondas cedo", 8, List.of("leve", "manhã")
        );
        when(surfSummaryService.getOrGenerateToday(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/summaries/spot/{spotId}/today", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Boas ondas cedo"))
                .andExpect(jsonPath("$.tags[1]").value("manhã"));
    }
}
