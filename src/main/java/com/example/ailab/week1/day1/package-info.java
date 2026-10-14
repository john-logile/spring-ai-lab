/**
 * Day 1 — the container creates objects; your code asks for them.
 *
 * <p>Three beans implement {@link com.example.ailab.week1.day1.WelcomeService}:
 * <ul>
 *   <li>{@code @Component} — component scan finds the class</li>
 *   <li>{@code @Bean} — a method in a {@code @Configuration} class returns the instance</li>
 *   <li>{@code @Configuration} class — {@code @Configuration} is itself a stereotype,
 *       so the configuration class is a bean and can be injected</li>
 * </ul>
 *
 * <p>{@link com.example.ailab.week1.day1.CircularLeft} and {@code CircularRight}
 * are {@code @Profile("circular")}. They are not created during a normal start.
 * Turn them on with {@code --spring.profiles.active=dev,circular} and read
 * {@code BeanCurrentlyInCreationException}.
 */
package com.example.ailab.week1.day1;
