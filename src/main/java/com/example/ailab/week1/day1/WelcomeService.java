package com.example.ailab.week1.day1;

/**
 * One service, three ways of registering it. Callers depend on this type,
 * never on which annotation created the instance.
 */
public interface WelcomeService {

    String welcome();

    /** How this bean was registered: {@code @Component}, {@code @Bean}, or {@code @Configuration}. */
    String definedBy();
}
