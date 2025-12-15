package com.surfmaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.dto.CreateChatMessageRequest;
import com.surfmaster.entities.ChatRole;
import com.surfmaster.service.ChatService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @Test
    void openSessionReturnsPayloadFromService() throws Exception {
        var createdAt = OffsetDateTime.now();
        var dto = new ChatSessionDto(1L, 5L, 7L, createdAt, List.of());
        when(chatService.openSession(7L, 5L)).thenReturn(dto);

        mockMvc.perform(post("/api/chat/sessions/{spotId}", 7L)
                        .param("userId", "5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.spotId").value(7))
                .andExpect(jsonPath("$.userId").value(5));
    }

    @Test
    void getSessionDelegatesToService() throws Exception {
        var createdAt = OffsetDateTime.now();
        var msg = new ChatMessageDto(3L, ChatRole.USER, 2L, "aloha", createdAt);
        var dto = new ChatSessionDto(2L, null, 8L, createdAt, List.of(msg));
        when(chatService.getSession(2L)).thenReturn(dto);

        mockMvc.perform(get("/api/chat/sessions/{sessionId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].content").value("aloha"))
                .andExpect(jsonPath("$.messages[0].chatRole").value("USER"));
    }

    @Test
    void sendMessagePassesRequestBodyToService() throws Exception {
        var createdAt = OffsetDateTime.now();
        var response = new ChatMessageDto(11L, ChatRole.USER, 9L, "Oi", createdAt);
        when(chatService.postUserMessage(org.mockito.ArgumentMatchers.any(CreateChatMessageRequest.class)))
                .thenReturn(response);

        var payload = new CreateChatMessageRequest(9L, ChatRole.USER, "Oi");

        mockMvc.perform(post("/api/chat/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Oi"));

        ArgumentCaptor<CreateChatMessageRequest> captor = ArgumentCaptor.forClass(CreateChatMessageRequest.class);
        verify(chatService).postUserMessage(captor.capture());
        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().chatSessionId()).isEqualTo(9L);
        assertThat(captor.getValue().chatRole()).isEqualTo(ChatRole.USER);
        assertThat(captor.getValue().content()).isEqualTo("Oi");
    }
}
