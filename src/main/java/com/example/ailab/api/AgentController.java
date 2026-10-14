package com.example.ailab.api;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * The "agent" endpoint: chat with tool calling enabled.
 *
 * <p>Try: {@code "What time is it in Tokyo right now, and what is (15/100)*240?"}
 * The model should call {@code currentDateTime} and {@code calculate}, then
 * compose the final answer. Watch the logs — tool calls are the moment an
 * LLM stops being a chatbot and starts being an agent.
 *
 * <p>{@code POST /api/agent} with {@code {"message": "..."}}
 *
 * <p>WebFlux note: the agent call blocks (see {@link ChatController}), so it is
 * offloaded to the {@code boundedElastic} pool — never block the event loop.
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    private final ChatClient chatClient;
    private final com.example.ailab.agent.AssistantTools tools;

    public AgentController(ChatClient chatClient, com.example.ailab.agent.AssistantTools tools) {
        this.chatClient = chatClient;
        this.tools = tools;
    }

    record AgentRequest(String message) {
    }

    @PostMapping
    public Mono<String> ask(@RequestBody AgentRequest request) {
        return Mono.fromCallable(() -> chatClient.prompt()
                        .system("""
                                You are a helpful assistant with access to tools.
                                When a question needs the current time, a calculation, or a currency
                                conversion, call the appropriate tool instead of guessing.
                                After tool results come back, answer in a friendly, concise way.
                                """)
                        .user(request.message())
                        .tools(tools) // <-- this one line turns the chat into an agent
                        .call()
                        .content())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
