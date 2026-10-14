package com.example.ailab.week1.day3;

import com.example.labautoconfigure.GreetingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GreetingOverrideConfig {

    @Bean
    GreetingService greetingService() {
        return new GreetingService() {
            @Override
            public String greet() {
                return "Hello from the application";
            }

            @Override
            public String origin() {
                return "user bean";
            }
        };
    }
}
