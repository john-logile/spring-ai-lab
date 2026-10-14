/**
 * Day 3 — the application override. {@code GreetingAutoConfiguration} is not
 * in this package on purpose: auto-config must not be component-scanned.
 * This {@code @Bean} is the user bean that makes {@code @ConditionalOnMissingBean} back off.
 */
package com.example.ailab.week1.day3;
