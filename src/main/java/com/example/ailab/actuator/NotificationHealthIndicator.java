package com.example.ailab.actuator;

import com.example.ailab.week1.day2.NotificationProperties;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health component named {@code notification}
 * (the {@code HealthIndicator} suffix is stripped).
 *
 * <p>Validation already refuses a blank API key at startup. This indicator
 * is the runtime view of the same rule, for a process that is already up.
 * It reports the mode, never the key.
 */
@Component
public class NotificationHealthIndicator implements HealthIndicator {

    private final NotificationProperties properties;

    public NotificationHealthIndicator(NotificationProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        boolean configured = properties.apiKey() != null && !properties.apiKey().isBlank();
        if (!configured) {
            return Health.down()
                    .withDetail("check", "notification configuration")
                    .withDetail("reason", "API key is missing")
                    .build();
        }
        return Health.up()
                .withDetail("check", "notification configuration")
                .withDetail("mode", properties.mode().name())
                .build();
    }
}
