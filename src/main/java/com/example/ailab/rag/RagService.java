package com.example.ailab.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The <strong>R</strong>etrieval + <strong>G</strong>eneration half of RAG.
 *
 * <p>Instead of letting the model answer from its training data (which may be
 * stale or hallucinated), we:
 * <ol>
 *   <li>search the vector store for chunks similar to the question,</li>
 *   <li>stuff them into the prompt as context,</li>
 *   <li>tell the model to answer <em>only</em> from that context.</li>
 * </ol>
 *
 * <p>This is retrieval-augmented generation in its simplest, most transparent form.
 * Spring AI also ships a one-line {@code QuestionAnswerAdvisor} that does the same
 * thing — build this first so you understand what the advisor hides.
 */
@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagService(ChatClient chatClient, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    public record RagAnswer(String answer, List<String> sources) {
    }

    public RagAnswer ask(String question) {
        // 1. Retrieve: top-4 most similar chunks
        List<Document> relevant = vectorStore.similaritySearch(question);

        String context = relevant.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        List<String> sources = relevant.stream()
                .map(doc -> String.valueOf(doc.getMetadata().getOrDefault("source", "unknown")))
                .distinct()
                .toList();

        // 2. Generate: answer strictly from the retrieved context
        String answer = chatClient.prompt()
                .system("""
                        Answer the user's question using ONLY the context below.
                        If the context does not contain the answer, say so honestly
                        instead of making something up.
                        Keep the answer concise.
                        """)
                .user("Context:\n" + context + "\n\nQuestion: " + question)
                .call()
                .content();

        return new RagAnswer(answer, sources);
    }
}
