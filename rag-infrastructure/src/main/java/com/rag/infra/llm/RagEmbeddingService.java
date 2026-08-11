package com.rag.infra.llm;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagEmbeddingService {

    private final EmbeddingModel embeddingModel;

    public RagEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String generateVectorString(String text) {
        // Calls the LLM to generate mathematical vectors for the text
        List<Double> vector = embeddingModel.embed(text);

        // Formats the array into a Postgres-compatible string: "[0.1, 0.2, ...]"
        return "[" + vector.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }
}