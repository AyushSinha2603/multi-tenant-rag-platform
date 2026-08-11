package com.rag.api.controller;

import com.rag.api.service.RagOrchestrationService;
import com.rag.infra.llm.RagEmbeddingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final RagOrchestrationService orchestrationService;
    private final RagEmbeddingService embeddingService;

    public ChatController(RagOrchestrationService orchestrationService, RagEmbeddingService embeddingService) {
        this.orchestrationService = orchestrationService;
        this.embeddingService = embeddingService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String prompt) {
        // Generate the real vector and pass it to the orchestrator
        String realEmbedding = embeddingService.generateVectorString(prompt);
        return orchestrationService.processQuery(prompt, realEmbedding);
    }
}