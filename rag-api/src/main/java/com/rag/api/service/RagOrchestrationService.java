package com.rag.api.service;

import com.rag.core.entity.Document;
import com.rag.infra.cache.RedisSemanticCacheService;
import com.rag.infra.llm.RagChatService;
import com.rag.infra.persistence.search.DocumentRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagOrchestrationService {

    private final RedisSemanticCacheService cacheService;
    private final DocumentRepository documentRepository;
    private final RagChatService chatService;

    public RagOrchestrationService(RedisSemanticCacheService cacheService,
                                   DocumentRepository documentRepository,
                                   RagChatService chatService) {
        this.cacheService = cacheService;
        this.documentRepository = documentRepository;
        this.chatService = chatService;
    }

    public Flux<String> processQuery(String prompt, String promptEmbedding) {
        String cacheKey = String.valueOf(prompt.hashCode());

        return cacheService.getCachedResponse(cacheKey)
                .map(Flux::just)
                .orElseGet(() -> fetchAndStream(prompt, promptEmbedding));
    }

    private Flux<String> fetchAndStream(String prompt, String embedding) {
        List<Document> documents = documentRepository.hybridSearch(prompt, embedding, 5, 60);

        String context = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        return chatService.streamResponse(prompt, context);
    }
}