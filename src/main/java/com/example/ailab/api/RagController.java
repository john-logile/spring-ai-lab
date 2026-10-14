package com.example.ailab.api;

import com.example.ailab.rag.DocumentLoader;
import com.example.ailab.rag.RagService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * RAG endpoints.
 *
 * <ul>
 *   <li>{@code POST /api/rag} — ask a question about the lab's own docs</li>
 *   <li>{@code POST /api/rag/reload} — re-ingest docs after editing them</li>
 * </ul>
 *
 * <p>Try: {@code "What does the agent module do?"} — the answer must come
 * from {@code src/main/resources/docs/}, not from the model's memory.
 *
 * <p>WebFlux note: retrieval + generation block (see {@link ChatController}),
 * so both endpoints are offloaded to the {@code boundedElastic} pool.
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;
    private final DocumentLoader documentLoader;

    public RagController(RagService ragService, DocumentLoader documentLoader) {
        this.ragService = ragService;
        this.documentLoader = documentLoader;
    }

    record RagRequest(String question) {
    }

    @PostMapping
    public Mono<RagService.RagAnswer> ask(@RequestBody RagRequest request) {
        return Mono.fromCallable(() -> ragService.ask(request.question()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    record ReloadResponse(int chunksLoaded) {
    }

    @PostMapping("/reload")
    public Mono<ReloadResponse> reload() {
        return Mono.fromCallable(() -> new ReloadResponse(documentLoader.loadDocuments()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
