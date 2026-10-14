package com.example.ailab.week1.day1;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Explicit Java config. The {@code @Bean} method is the factory;
 * its name is the bean name.
 */
@Configuration
public class WelcomeBeanConfig {

    @Bean
    WelcomeService beanMethodWelcomeService() {
        return new BeanMethodWelcomeService();
    }
}
