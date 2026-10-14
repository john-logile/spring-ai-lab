package com.example.labautoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * A tiny auto-configuration, same shape as Boot's own.
 *
 * <p>This class lives in {@code com.example.labautoconfigure}, outside
 * {@code com.example.ailab}, so component scan does not pick it up.
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * is what loads it. Auto-configurations run after user {@code @Configuration},
 * which is why {@code @ConditionalOnMissingBean} can see a bean the user defined.
 *
 * <p>Open {@code DataSourceAutoConfiguration} in the IDE (it arrives with
 * {@code spring-boot-starter-jdbc}) and compare: {@code @ConditionalOnClass}
 * checks the classpath, {@code @ConditionalOnMissingBean} backs off when you
 * defined a {@code DataSource}, {@code @ConditionalOnProperty} reads a flag.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "lab.greeting", name = "enabled", matchIfMissing = true)
public class GreetingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(GreetingService.class)
    GreetingService autoConfiguredGreetingService() {
        return new AutoConfiguredGreetingService();
    }
}
