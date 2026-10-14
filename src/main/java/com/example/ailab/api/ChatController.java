package com.example.ailab.api;

import com.example.ailab.prompt.PromptService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Plain chat endpoints: the "hello world" of Spring AI.
 *
 * <ul>
 *   <li>{@code POST /api/chat} — single request/response</li>
 *   <li>{@code GET /api/chat/stream?message=...} — token streaming over SSE</li>
 *   <li>{@code POST /api/chat/explain} — prompt-template example</li>
 *   <li>{@code POST /api/chat/review} — system+user message example</li>
 * </ul>
 *
 * <p><strong>WebFlux rule #1: never block the event-loop thread.</strong>
 * Spring AI's model calls are blocking (Ollama uses a blocking HTTP client),
 * but this app runs on Netty, whose event-loop threads refuse blocking work
 * ({@code block()... not supported in thread reactor-http-nio-...}).
 * So every blocking AI call below is shifted onto the {@code boundedElastic}
 * pool via {@code Mono.fromCallable(...).subscribeOn(...)}.
 * The {@code /stream} endpoint needs no such wrapping — it's reactive end to end.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatClient chatClient;
    private final PromptService promptService;

    public ChatController(ChatClient chatClient, PromptService promptService) {
        this.chatClient = chatClient;
        this.promptService = promptService;
    }

    record ChatRequest(String message) {
    }

    @PostMapping
    public Mono<String> chat(@RequestBody ChatRequest request) {
        return Mono.fromCallable(() -> chatClient.prompt()
                        .user(request.message())
                        .call()
                        .content())
                .subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }

    record ExplainRequest(String topic, String level) {
    }

    @PostMapping("/explain")
    public Mono<String> explain(@RequestBody ExplainRequest request) {
        String level = (request.level() == null || request.level().isBlank())
                ? "curious beginner" : request.level();
        return Mono.fromCallable(() -> promptService.explain(request.topic(), level))
                .subscribeOn(Schedulers.boundedElastic());
    }

    record ReviewRequest(String code) {
    }

    @PostMapping("/review")
    public Mono<String> review(@RequestBody ReviewRequest request) {
        return Mono.fromCallable(() -> promptService.roastMyCode(request.code()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
