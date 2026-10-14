package com.example.ailab.week1.day2;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Active only when the {@code dev} profile is on. */
@Component
@Profile("dev")
public class LogNotifier implements Notifier {

    private final NotificationProperties properties;

    public LogNotifier(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public String deliver(String message) {
        return "log[" + properties.sender() + "]: " + message;
    }
}
