package com.rag.infra.llm;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class RagChatService {

    private final ChatClient chatClient;

    public RagChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public Flux<String> streamResponse(String userPrompt, String retrievedContext) {
        return chatClient.prompt()
                .system("You are a highly accurate enterprise AI assistant. Answer the user's question using strictly the provided context. If the answer is not contained in the context, say so.")
                .user(u -> u.text("Context: {context}\n\nQuestion: {prompt}")
                        .param("context", retrievedContext)
                        .param("prompt", userPrompt))
                .stream()
                .content();
    }
}