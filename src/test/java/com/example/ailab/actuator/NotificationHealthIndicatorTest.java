package com.example.ailab.actuator;

import com.example.ailab.week1.day2.NotificationMode;
import com.example.ailab.week1.day2.NotificationProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationHealthIndicatorTest {

    @Test
    void missingKeyIsDownAndARealKeyIsUpWithoutLeakingIt() {
        var down = new NotificationHealthIndicator(
                new NotificationProperties("  ", NotificationMode.LOG, "dev@localhost")).health();
        assertThat(down.getStatus().getCode()).isEqualTo("DOWN");

        var up = new NotificationHealthIndicator(
                new NotificationProperties("secret", NotificationMode.EMAIL, "alerts@example.com")).health();
        assertThat(up.getStatus().getCode()).isEqualTo("UP");
        assertThat(up.getDetails()).containsEntry("mode", "EMAIL");
        assertThat(up.getDetails().values()).noneMatch(value -> String.valueOf(value).contains("secret"));
    }
}
