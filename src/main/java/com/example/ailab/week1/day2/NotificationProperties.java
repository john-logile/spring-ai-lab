package com.example.ailab.week1.day2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe view of {@code notify.*}.
 *
 * <p>Prefer this over {@code @Value}:
 * <ul>
 *   <li>relaxed binding — {@code notify.api-key}, {@code NOTIFY_API_KEY}, and
 *       {@code notify.apiKey} all land in {@link #apiKey()};</li>
 *   <li>{@code @Validated} plus Jakarta annotations fail startup when the
 *       value is missing, instead of failing on the first request;</li>
 *   <li>the configuration processor writes IDE metadata for these keys.</li>
 * </ul>
 *
 * <p>Registered by {@link Day2Config}, not by component scan. A
 * {@code @ConfigurationProperties} type is not a bean until something enables it.
 */
@ConfigurationProperties(prefix = "notify")
@Validated
public record NotificationProperties(
        @NotBlank String apiKey,
        @NotNull NotificationMode mode,
        @NotBlank String sender
) {
}
