package com.example.ailab.week1.day1;

/**
 * Way 2: not a stereotype. Nothing creates this class unless a {@code @Bean}
 * method returns it. That is useful when you do not own the class
 * (a third-party client, a JDK type).
 */
public class BeanMethodWelcomeService implements WelcomeService {

    @Override
    public String welcome() {
        return "Welcome from a @Bean method";
    }

    @Override
    public String definedBy() {
        return "@Bean";
    }
}
