package com.example.ailab.week1.day1;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Constructor injection of every {@link WelcomeService}.
 *
 * <p>Field injection ({@code @Autowired} on a field) is banned here because:
 * <ul>
 *   <li>the dependency can be reassigned, so the object is not immutable;</li>
 *   <li>a missing dependency stays hidden until something calls the field;</li>
 *   <li>tests must start a container or use reflection to set the field.
 *       A constructor can be called with {@code new} and a fake.</li>
 * </ul>
 * A single constructor does not need {@code @Autowired}. Spring uses it.
 */
@Service
public class WelcomeBoard {

    private final List<WelcomeService> services;

    public WelcomeBoard(List<WelcomeService> services) {
        this.services = List.copyOf(services);
    }

    public List<String> lines() {
        return services.stream()
                .map(service -> service.definedBy() + " — " + service.welcome())
                .sorted()
                .toList();
    }
}
