package com.example.ailab.week1.day2;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Prototype: a new instance every time someone asks the container.
 * The id is assigned in the constructor, so two asks produce two ids.
 *
 * <p>Request and session scopes exist for servlet apps ({@code @RequestScope},
 * {@code @SessionScope}). This lab is WebFlux, which has no servlet request
 * scope, so the scope you can see here is prototype versus the singleton default.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NotificationDraft {

    private final String id = UUID.randomUUID().toString();

    public String id() {
        return id;
    }
}
