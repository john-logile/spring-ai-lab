package com.example.ailab.week1.day2;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Active only when the {@code prod} profile is on. No network call — the point is the wiring. */
@Component
@Profile("prod")
public class EmailNotifier implements Notifier {

    private final NotificationProperties properties;

    public EmailNotifier(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String deliver(String message) {
        return "email[" + properties.sender() + "]: " + message;
    }
}
