package com.rag.api.controller;

import com.rag.api.service.RagOrchestrationService;
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

    public ChatController(RagOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String prompt) {
        String mockEmbedding = "[0.0, 0.0, 0.0]";
        return orchestrationService.processQuery(prompt, mockEmbedding);
    }
}