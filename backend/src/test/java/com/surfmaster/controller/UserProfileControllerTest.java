package com.surfmaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surfmaster.dto.UpsertUserProfileRequest;
import com.surfmaster.dto.UserProfileDto;
import com.surfmaster.entities.BoardType;
import com.surfmaster.entities.UserLevel;
import com.surfmaster.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileController.class)
class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    @Test
    void listProfilesReturnsDtos() throws Exception {
        var dto = new UserProfileDto(1L, "Edu", UserLevel.BEGINNER, List.of(BoardType.SHORTBOARD));
        when(userProfileService.listProfiles()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Edu"));
    }

    @Test
    void getProfileDelegatesToService() throws Exception {
        var dto = new UserProfileDto(5L, "Ana", UserLevel.ADVANCED, List.of());
        when(userProfileService.getProfile(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/profile/{id}", 5L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value("ADVANCED"));
    }

    @Test
    void createProfilePassesBody() throws Exception {
        var request = new UpsertUserProfileRequest("Mar", UserLevel.INTERMEDIATE, List.of(BoardType.FUNBOARD));
        var response = new UserProfileDto(9L, "Mar", UserLevel.INTERMEDIATE, List.of(BoardType.FUNBOARD));
        when(userProfileService.createProfile(request)).thenReturn(response);

        mockMvc.perform(post("/api/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));
    }

    @Test
    void updateProfileSendsRequestToService() throws Exception {
        var request = new UpsertUserProfileRequest("Gui", UserLevel.BEGINNER, List.of());
        var response = new UserProfileDto(3L, "Gui", UserLevel.BEGINNER, List.of());
        when(userProfileService.updateProfile(3L, request)).thenReturn(response);

        mockMvc.perform(put("/api/profile/{id}", 3L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Gui"));
    }

    @Test
    void deleteProfileCallsService() throws Exception {
        mockMvc.perform(delete("/api/profile/{id}", 12L))
                .andExpect(status().isOk());

        verify(userProfileService).deleteProfile(12L);
    }
}
