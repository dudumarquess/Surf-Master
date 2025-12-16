package com.surfmaster.service;

import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.dto.CreateChatMessageRequest;
import com.surfmaster.entities.ChatMessage;
import com.surfmaster.entities.ChatRole;
import com.surfmaster.entities.ChatSession;
import com.surfmaster.entities.Spot;
import com.surfmaster.entities.UserProfile;
import com.surfmaster.repository.ChatMessageRepository;
import com.surfmaster.repository.ChatSessionRepository;
import com.surfmaster.repository.SpotRepository;
import com.surfmaster.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private SpotRepository spotRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private SurfAssistantService surfAssistantService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void openSessionCreatesSessionWithSpotAndUser() {
        var spot = Spot.builder().id(8L).build();
        var user = UserProfile.builder().id(5L).build();
        when(spotRepository.findById(8L)).thenReturn(Optional.of(spot));
        when(userProfileRepository.findById(5L)).thenReturn(Optional.of(user));
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.setId(99L);
            return session;
        });

        ChatSessionDto dto = chatService.openSession(8L, 5L);

        assertThat(dto.spotId()).isEqualTo(8L);
        assertThat(dto.userId()).isEqualTo(5L);
        assertThat(dto.id()).isEqualTo(99L);

        ArgumentCaptor<ChatSession> captor = ArgumentCaptor.forClass(ChatSession.class);
        verify(chatSessionRepository).save(captor.capture());
        assertThat(captor.getValue().getSpot()).isEqualTo(spot);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }

    @Test
    void getSessionReturnsMessagesOrderedFromEntity() {
        var spot = Spot.builder().id(4L).build();
        var session = ChatSession.builder()
                .id(2L)
                .spot(spot)
                .createdAt(OffsetDateTime.now().minusMinutes(5))
                .build();
        var message = ChatMessage.builder()
                .id(10L)
                .chatSession(session)
                .chatRole(ChatRole.USER)
                .content("Bom dia")
                .createdAt(OffsetDateTime.now())
                .build();
        session.setMessages(List.of(message));
        when(chatSessionRepository.findByIdWithMessages(2L)).thenReturn(Optional.of(session));

        ChatSessionDto dto = chatService.getSession(2L);

        assertThat(dto.messages()).hasSize(1);
        ChatMessageDto dtoMessage = dto.messages().get(0);
        assertThat(dtoMessage.content()).isEqualTo("Bom dia");
        assertThat(dtoMessage.chatRole()).isEqualTo(ChatRole.USER);
    }

    @Test
    void postUserMessagePersistsMessage() {
        var session = ChatSession.builder().id(6L).build();
        when(chatSessionRepository.findById(6L)).thenReturn(Optional.of(session));
        var storedMessages = new java.util.ArrayList<ChatMessage>();
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage entity = invocation.getArgument(0);
            entity.setId((long) (storedMessages.size() + 1));
            storedMessages.add(entity);
            return entity;
        });
        when(chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(6L))
                .thenAnswer(invocation -> storedMessages);
        when(surfAssistantService.respond(any(), any(), any())).thenReturn("Olá, iniciante, experimente Carcavelos!");

        ChatMessageDto dto = chatService.postUserMessage(new CreateChatMessageRequest(6L, ChatRole.USER, "fala aí"));

        assertThat(dto.chatRole()).isEqualTo(ChatRole.ASSISTANT);
        assertThat(dto.content()).contains("Carcavelos");
        verify(chatMessageRepository, org.mockito.Mockito.times(2)).save(any(ChatMessage.class));
        verify(surfAssistantService).respond(any(), any(), any());
    }

    @Test
    void resetSessionUpdatesSpotUserAndClearsHistory() {
        var oldSpot = Spot.builder().id(1L).build();
        var oldUser = UserProfile.builder().id(2L).build();
        var session = ChatSession.builder()
                .id(12L)
                .spot(oldSpot)
                .user(oldUser)
                .createdAt(OffsetDateTime.now().minusHours(1))
                .build();
        session.setMessages(new ArrayList<>(List.of(ChatMessage.builder().id(1L).build())));

        var newSpot = Spot.builder().id(9L).build();
        var newUser = UserProfile.builder().id(7L).build();

        when(chatSessionRepository.findById(12L)).thenReturn(Optional.of(session));
        when(spotRepository.findById(9L)).thenReturn(Optional.of(newSpot));
        when(userProfileRepository.findById(7L)).thenReturn(Optional.of(newUser));
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionDto dto = chatService.resetSession(12L, 9L, 7L);

        assertThat(dto.spotId()).isEqualTo(9L);
        assertThat(dto.userId()).isEqualTo(7L);
        assertThat(dto.messages()).isEmpty();
        verify(chatMessageRepository).deleteByChatSessionId(12L);
        assertThat(session.getMessages()).isEmpty();
    }
}
