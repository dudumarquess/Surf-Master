package com.surfmaster.llm.huggingface;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.surfmaster.config.LlmProperties;
import com.surfmaster.llm.EmbeddingClient;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class HuggingFaceEmbeddingClient implements EmbeddingClient {

    private final WebClient client;
    private final LlmProperties properties;

    public HuggingFaceEmbeddingClient(WebClient.Builder builder, LlmProperties properties) {
        this.properties = properties;
        this.client = builder
                .clientConnector(new ReactorClientHttpConnector())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public double[] embed(String text) {
        if (properties.getEmbeddingApiKey() == null || properties.getEmbeddingApiKey().isBlank()) {
            throw new IllegalStateException("SURF_HF_API_KEY not configured for embeddings.");
        }
        if (text == null || text.isBlank()) {
            return new double[16];
        }

        String endpoint = resolveEndpoint();
        Map<String, Object> payload = buildPayload(endpoint, text);

        log.debug("HuggingFace embedding request length={}", text.length());
        JsonNode response = client.post()
                .uri(endpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getEmbeddingApiKey())
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.isError(), resp -> resp.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new IllegalStateException("HuggingFace embeddings error: " + body))))
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(60));

        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("HuggingFace embeddings returned empty response");
        }

        List<List<Double>> tokens = normalizeEmbeddings(response);
        int dimensions = tokens.get(0).size();
        double[] vector = new double[dimensions];
        for (List<Double> token : tokens) {
            for (int i = 0; i < dimensions; i++) {
                vector[i] += token.get(i);
            }
        }
        for (int i = 0; i < dimensions; i++) {
            vector[i] /= tokens.size();
        }
        return vector;
    }

    private Map<String, Object> buildPayload(String endpoint, String text) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("inputs", text);
        payload.put("options", Map.of("wait_for_model", true));
        return payload;
    }

    private List<List<Double>> normalizeEmbeddings(JsonNode node) {
        List<List<Double>> vectors = new ArrayList<>();
        if (node.isArray()) {
            if (!node.isEmpty() && node.get(0).isArray()) {
                for (JsonNode arr : node) {
                    vectors.add(toDoubleList(arr));
                }
            } else {
                vectors.add(toDoubleList(node));
            }
        } else {
            throw new IllegalStateException("Unexpected embedding payload: " + node);
        }
        return vectors;
    }

    private List<Double> toDoubleList(JsonNode arr) {
        List<Double> values = new ArrayList<>();
        arr.forEach(item -> values.add(item.asDouble()));
        return values;
    }

    private String resolveEndpoint() {
        String endpoint = properties.getEmbeddingEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("HuggingFace embedding endpoint not configured");
        }
        if (endpoint.contains("pipeline/sentence-similarity")) {
            endpoint = endpoint.replace("pipeline/sentence-similarity", "pipeline/feature-extraction");
        }
        if (endpoint.contains("{model}")) {
            endpoint = endpoint.replace("{model}", properties.getEmbeddingModel());
        }
        if (endpoint.startsWith("https://api-inference.huggingface.co")) {
            endpoint = endpoint.replace("https://api-inference.huggingface.co", "https://router.huggingface.co");
        }
        return endpoint;
    }
}
