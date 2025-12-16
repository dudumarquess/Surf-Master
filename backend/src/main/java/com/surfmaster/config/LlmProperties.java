package com.surfmaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "surf.llm")
public class LlmProperties {
    @NotBlank
    private String provider = "groq";
    @NotBlank
    private String model = "llama-3.1-8b-instant";
    private String embeddingModel = "BAAI/bge-base-en-v1.5";
    private String apiKey;
    private String baseUrl = "https://api.groq.com/openai/v1";
    private double temperature = 0.2;
    private int maxTokens = 400;
    private String embeddingProvider = "huggingface";
    private String embeddingEndpoint = "https://api-inference.huggingface.co/models/BAAI/bge-base-en-v1.5";
    private String embeddingApiKey;
}
