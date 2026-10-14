package com.example.ailab.week1.day1;

import org.springframework.context.annotation.Configuration;

/**
 * Way 3: the configuration class is the service.
 *
 * <p>{@code @Configuration} is a {@code @Component}, so component scan registers
 * this class as a bean. Spring then subclasses it (CGLIB) so {@code @Bean}
 * methods can be intercepted. It can still be injected as {@link WelcomeService}.
 */
@Configuration
public class ConfigurationWelcomeService implements WelcomeService {

    @Override
    public String welcome() {
        return "Welcome from a @Configuration class";
    }

    @Override
    public String definedBy() {
        return "@Configuration";
    }
}
