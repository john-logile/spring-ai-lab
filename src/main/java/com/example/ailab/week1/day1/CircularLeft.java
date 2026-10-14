package com.example.ailab.week1.day1;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Constructor cycle, on purpose. Left needs Right, Right needs Left, and
 * neither can be constructed first. Spring's default
 * ({@code spring.main.allow-circular-references=false}) refuses the loop
 * with {@code BeanCurrentlyInCreationException}.
 *
 * <p>Inactive unless the {@code circular} profile is on, so a normal start
 * and {@code ApplicationTests.contextLoads()} stay green.
 */
@Component
@Profile("circular")
public class CircularLeft {

    public CircularLeft(CircularRight right) {
        Objects.requireNonNull(right);
    }
}
