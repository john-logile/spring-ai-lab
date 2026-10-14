package com.example.ailab.week1.day1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircularDependencyTest {

    @Test
    void constructorCycleFailsWhileTheBeanIsStillBeingCreated() {
        assertThatThrownBy(() -> {
            var context = new AnnotationConfigApplicationContext();
            context.getEnvironment().setActiveProfiles("circular");
            context.scan("com.example.ailab.week1.day1");
            context.refresh();
        }).hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class);
    }
}
