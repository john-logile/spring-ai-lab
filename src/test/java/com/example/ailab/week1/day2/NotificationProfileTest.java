package com.example.ailab.week1.day2;

import com.example.ailab.AiLabApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationProfileTest {

    @Test
    void prodProfileSwitchesTheNotifierWithoutACodeChange() {
        try (ConfigurableApplicationContext context = SpringApplication.run(
                AiLabApplication.class,
                "--spring.profiles.active=prod",
                "--spring.main.web-application-type=none",
                "--spring.ai.ollama.init.pull-model-strategy=never")) {
            Notifier notifier = context.getBean(Notifier.class);
            assertThat(notifier).isInstanceOf(EmailNotifier.class);
            assertThat(notifier.deliver("hi")).isEqualTo("email[alerts@example.com]: hi");
        }
    }

    @Test
    void blankApiKeyFailsStartup() {
        assertThatThrownBy(() -> SpringApplication.run(
                AiLabApplication.class,
                "--spring.profiles.active=broken",
                "--spring.main.web-application-type=none",
                "--spring.ai.ollama.init.pull-model-strategy=never"))
                .hasStackTraceContaining("apiKey");
    }
}
