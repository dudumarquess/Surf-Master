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
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

//MVP without LLM yet
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SpotRepository spotRepository;
    private final UserProfileRepository userProfileRepository;

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

    public ChatMessageDto postUserMessage(CreateChatMessageRequest request) {
        var s = chatSessionRepository.findById(request.chatSessionId()).orElseThrow();
        var m = ChatMessage.builder()
                .chatSession(s)
                .chatRole(request.chatRole())
                .content(request.content())
                .createdAt(OffsetDateTime.now())
                .build();
        m = chatMessageRepository.save(m);

        // TODO: call LLM and Append assistant message
        return ChatMapper.toDto(m);
    }

}
