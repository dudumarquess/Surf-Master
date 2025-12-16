package com.surfmaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surfmaster.dto.RecommendationRequest;
import com.surfmaster.dto.RecommendationResponse;
import com.surfmaster.entities.Objective;
import com.surfmaster.entities.Reason;
import com.surfmaster.entities.ReasonType;
import com.surfmaster.entities.Risk;
import com.surfmaster.entities.RiskType;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.dto.RecommendationItem;
import com.surfmaster.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    @Test
    void recommendReturnsServicePayload() throws Exception {
        var now = OffsetDateTime.now();
        var request = new RecommendationRequest(
                -43.2, -22.9, UserLevel.INTERMEDIATE, Objective.FUN,
                30.0, now, now.plusHours(5), 3, 2
        );
        var item = new RecommendationItem(
                10L, "Leme", now, now.plusHours(1), now.plusMinutes(45),
                82.5, List.of(new Reason(ReasonType.SWELL, "Swell alinhado")),
                List.of(new Risk(RiskType.LOW_CONFIDENCE, "Margem de erro alta")), 0.75
        );
        var response = new RecommendationResponse(now, request.timeStart(), request.timeEnd(), List.of(item));
        when(recommendationService.recommend(any())).thenReturn(response);

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations[0].spotName").value("Leme"))
                .andExpect(jsonPath("$.recommendations[0].score").value(82.5));
    }

    @Test
    void recommendHandlesServiceValidationError() throws Exception {
        when(recommendationService.recommend(any())).thenThrow(new IllegalArgumentException("Invalid time range"));

        var request = new RecommendationRequest(
                -43.2, -22.9, UserLevel.BEGINNER, Objective.FUN,
                30.0, OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), 3, 1
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid time range"));
    }

    @Test
    void recommendFailsOnMissingLatitude() throws Exception {
        var request = new RecommendationRequest(
                -43.2, null, UserLevel.BEGINNER, Objective.FUN,
                30.0, OffsetDateTime.now(), OffsetDateTime.now().plusHours(1), 3, 1
        );

        mockMvc.perform(post("/api/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
