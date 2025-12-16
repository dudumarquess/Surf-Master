package com.surfmaster.service;

import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.dto.CreateChatMessageRequest;
import com.surfmaster.entities.*;
import com.surfmaster.mappers.ChatMapper;
import com.surfmaster.repository.ChatMessageRepository;
import com.surfmaster.repository.ChatSessionRepository;
import com.surfmaster.repository.SpotRepository;
import com.surfmaster.repository.UserProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SpotRepository spotRepository;
    private final UserProfileRepository userProfileRepository;
    private final SurfAssistantService surfAssistantService;

    @Transactional
    public ChatSessionDto openSession(Long spotId, Long userId) {
        Spot spot = spotRepository.findById(spotId).orElseThrow();
        UserProfile user = (userId == null) ? null : userProfileRepository.findById(userId).orElse(null);

        ChatSession chatSession = ChatSession.builder()
                .spot(spot)
                .user(user)
                .createdAt(OffsetDateTime.now())
                .build();

        chatSession = chatSessionRepository.save(chatSession);

        return ChatMapper.toDto(chatSession, List.of());
    }

    public ChatSessionDto getSession(Long sessionId) {
        var s = chatSessionRepository.findByIdWithMessages(sessionId).orElseThrow();
        var messages = s.getMessages().stream().map(ChatMapper::toDto).toList();
        return ChatMapper.toDto(s, messages);
    }

    @Transactional
    public ChatSessionDto resetSession(Long sessionId, Long spotId, Long userId) {
        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow();
        Spot newSpot = spotRepository.findById(spotId).orElseThrow();
        UserProfile newUser = null;
        if (userId != null) {
            newUser = userProfileRepository.findById(userId).orElseThrow();
        }

        session.setSpot(newSpot);
        session.setUser(newUser);
        session.setCreatedAt(OffsetDateTime.now());

        chatMessageRepository.deleteByChatSessionId(sessionId);
        if (session.getMessages() != null) {
            session.getMessages().clear();
        }

        ChatSession saved = chatSessionRepository.save(session);
        return ChatMapper.toDto(saved, List.of());
    }

    @Transactional
    public ChatMessageDto postUserMessage(CreateChatMessageRequest request) {
        ChatSession session = chatSessionRepository.findById(request.chatSessionId()).orElseThrow();

        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .chatRole(request.chatRole())
                .content(request.content())
                .createdAt(OffsetDateTime.now())
                .build();
        chatMessageRepository.save(userMessage);

        List<ChatMessage> history = chatMessageRepository.findByChatSessionIdOrderByCreatedAtAsc(session.getId());

        String assistantAnswer = buildAssistantAnswer(session, history, request.content());

        ChatMessage assistant = ChatMessage.builder()
                .chatSession(session)
                .chatRole(ChatRole.ASSISTANT)
                .content(assistantAnswer)
                .createdAt(OffsetDateTime.now())
                .build();
        assistant = chatMessageRepository.save(assistant);

        return ChatMapper.toDto(assistant);
    }

    private String buildAssistantAnswer(ChatSession session, List<ChatMessage> history, String userContent) {
        try {
            return surfAssistantService.respond(session, history, userContent);
        } catch (Exception e) {
            log.error("Failed to call the Surf Master", e);
            return "I could not reach the Surf Master right now. Please try again shortly.";
        }
    }

}
