package com.example.ailab.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central AI wiring.
 *
 * <p>Spring Boot auto-configures the Ollama {@code ChatModel} and {@code EmbeddingModel}
 * from {@code application.properties}. Here we build the two beans every module needs:
 * a shared {@link ChatClient} and an in-memory {@link VectorStore} for RAG.
 */
@Configuration
public class AiConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a friendly teaching assistant inside a Spring AI learning lab.
                        Keep answers concise unless the user asks for detail.
                        """)
                .build();
    }

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        // In-memory store: perfect for learning, no database required.
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
