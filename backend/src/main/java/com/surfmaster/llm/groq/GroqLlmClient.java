package com.surfmaster.llm.groq;

import java.time.Duration;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.surfmaster.config.LlmProperties;
import com.surfmaster.llm.LlmChatOptions;
import com.surfmaster.llm.LlmClient;
import com.surfmaster.llm.LlmCompletionResult;
import com.surfmaster.llm.LlmMessage;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GroqLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final WebClient client;

    public GroqLlmClient(WebClient.Builder builder, LlmProperties properties) {
        this.properties = properties;
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("SURF_LLM_API_KEY not configured. Set it to enable LLM features.");
        }
        this.client = builder
                .baseUrl(properties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public LlmCompletionResult chat(List<LlmMessage> messages, LlmChatOptions options) {
        log.debug("Groq chat request with {} messages", messages.size());
        GroqChatRequest request = new GroqChatRequest(
                properties.getModel(),
                messages.stream().map(m -> new GroqChatRequest.Message(m.role(), m.content())).toList(),
                options != null ? options.temperature() : properties.getTemperature(),
                options != null ? options.maxTokens() : properties.getMaxTokens()
        );

        GroqChatResponse response = client.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GroqChatResponse.class)
                .block(Duration.ofSeconds(30));

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Groq chat returned no choices");
        }

        String content = response.choices().get(0).message().content();
        double tokens = response.usage() != null ? response.usage().totalTokens() : 0;
        return new LlmCompletionResult(content, tokens);
    }

}
