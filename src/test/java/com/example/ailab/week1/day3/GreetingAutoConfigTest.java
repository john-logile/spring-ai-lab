package com.example.ailab.week1.day3;

import com.example.labautoconfigure.GreetingAutoConfiguration;
import com.example.labautoconfigure.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingAutoConfigTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreetingAutoConfiguration.class));

    @Test
    void autoConfigSuppliesAGreetingWhenTheUserDoesNot() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(GreetingService.class);
            assertThat(context.getBean(GreetingService.class).origin()).isEqualTo("auto-config");
        });
    }

    @Test
    void userBeanMakesAutoConfigBackOff() {
        runner.withUserConfiguration(GreetingOverrideConfig.class).run(context -> {
            assertThat(context).hasSingleBean(GreetingService.class);
            assertThat(context.getBean(GreetingService.class).origin()).isEqualTo("user bean");
        });
    }

    @Test
    void propertyTurnsTheAutoConfigOff() {
        runner.withPropertyValues("lab.greeting.enabled=false").run(context ->
                assertThat(context).doesNotHaveBean(GreetingService.class));
    }

    @Test
    void importsFileRegistersTheAutoConfig() throws Exception {
        String imports = new ClassPathResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports")
                .getContentAsString(StandardCharsets.UTF_8);
        assertThat(imports).contains(GreetingAutoConfiguration.class.getName());
    }
}
