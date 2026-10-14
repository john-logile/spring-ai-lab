package com.example.ailab.prompt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Demonstrates {@link PromptTemplate}: reusable prompts with {placeholders}.
 *
 * <p>Prompt engineering 101: a good template separates the <em>instruction</em>
 * (system message) from the <em>input</em> (user message) and names its variables.
 */
@Service
public class PromptService {

    private final ChatClient chatClient;

    public PromptService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * Explains any topic at a chosen level, using a template with placeholders.
     */
    public String explain(String topic, String level) {
        var template = new PromptTemplate("""
                Explain the following topic so that a {level} can understand it.
                Use a short analogy, then list the 3 most important ideas.

                Topic: {topic}
                """);

        var prompt = template.create(Map.of(
                "topic", topic,
                "level", level));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * Shows the classic system + user message split done explicitly.
     */
    public String roastMyCode(String code) {
        return chatClient.prompt()
                .system("""
                        You are a senior Java reviewer with a dry sense of humor.
                        Review the code for bugs and Spring anti-patterns.
                        Be kind but honest. End with one concrete suggestion.
                        """)
                .user("Review this code:\n\n" + code)
                .call()
                .content();
    }
}
