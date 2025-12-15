package com.surfmaster.mappers;

import com.surfmaster.dto.ChatMessageDto;
import com.surfmaster.dto.ChatSessionDto;
import com.surfmaster.entities.ChatMessage;
import com.surfmaster.entities.ChatSession;

import java.util.List;

public class ChatMapper {

    public static ChatMessageDto toDto(ChatMessage msg) {
        return new ChatMessageDto(
                msg.getId(),
                msg.getChatRole(),
                msg.getChatSession().getId(),
                msg.getContent(),
                msg.getCreatedAt()
        );
    }

    public static ChatSessionDto toDto(ChatSession session, List<ChatMessageDto> messages) {
        return new ChatSessionDto(
                session.getId(),
                session.getUser() != null ? session.getUser().getId() : null,
                session.getSpot() != null ? session.getSpot().getId() : null,
                session.getCreatedAt(),
                messages
        );
    }
}
