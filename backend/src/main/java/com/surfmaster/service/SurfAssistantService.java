package com.surfmaster.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.surfmaster.entities.ChatMessage;
import com.surfmaster.entities.ChatRole;
import com.surfmaster.entities.ChatSession;
import com.surfmaster.llm.LlmChatOptions;
import com.surfmaster.llm.LlmClient;
import com.surfmaster.llm.LlmCompletionResult;
import com.surfmaster.llm.LlmMessage;
import com.surfmaster.service.rag.RagContext;
import com.surfmaster.service.rag.SpotRagService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurfAssistantService {

    private static final String SYSTEM_PROMPT = """
            You are the Surf Master, a Brazilian expert on surf spots.
            Rules:
            - Always respond in clear and concise English.
            - Use only the provided context. If there is not enough information, say that explicitly.
            - Provide practical guidance about swell, wind, and surfer level.
            - Whenever you mention a spot, briefly explain why it matches the request.
            """;

    private final SpotRagService spotRagService;
    private final LlmClient llmClient;

    public String respond(ChatSession session, List<ChatMessage> history, String userMessage) {
        log.info("LLM request: session={}, question='{}'", session.getId(), userMessage);
        Long activeSpotId = session.getSpot() != null ? session.getSpot().getId() : null;
        RagContext context = spotRagService.retrieveContext(userMessage, activeSpotId);
        log.debug("RAG context for session {}: {} spots", session.getId(), context.spots().size());

        StringBuilder system = new StringBuilder(SYSTEM_PROMPT);
        system.append("\n\nCONTEXT:\n").append(context.buildContextBlock());
        if (context.usedFallback()) {
            system.append("\n\nIMPORTANT NOTE: ").append(context.fallbackReason())
                    .append(" Let the user know that the recommendations may be more generic.");
        }

        List<LlmMessage> llmMessages = new ArrayList<>();
        llmMessages.add(LlmMessage.system(system.toString()));

        if (history != null) {
            history.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .limit(15)
                    .forEach(msg -> llmMessages.add(new LlmMessage(roleToApi(msg.getChatRole()), msg.getContent())));
        }

        llmMessages.add(LlmMessage.user(userMessage));

        LlmChatOptions opts = LlmChatOptions.builder().temperature(0.2).maxTokens(400).build();
        LlmCompletionResult completion = llmClient.chat(llmMessages, opts);
        log.info("LLM response tokens={} session={}", completion.tokenCost(), session.getId());
        return completion.content();
    }

    private String roleToApi(ChatRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
        };
    }
}
