package com.example.ailab.week1.day1;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile("circular")
public class CircularRight {

    public CircularRight(CircularLeft left) {
        Objects.requireNonNull(left);
    }
}
