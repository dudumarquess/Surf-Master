package com.surfmaster.llm;

public interface EmbeddingClient {
    double[] embed(String text);
}
