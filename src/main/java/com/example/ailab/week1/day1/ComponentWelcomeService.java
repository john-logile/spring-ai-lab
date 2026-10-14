package com.example.ailab.week1.day1;

import org.springframework.stereotype.Component;

/**
 * Way 1: component scanning. {@code @SpringBootApplication} scans
 * {@code com.example.ailab} and registers every stereotype it finds.
 */
@Component
public class ComponentWelcomeService implements WelcomeService {

    @Override
    public String welcome() {
        return "Welcome from component scan";
    }

    @Override
    public String definedBy() {
        return "@Component";
    }
}
