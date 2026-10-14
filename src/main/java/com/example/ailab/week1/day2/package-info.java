/**
 * Day 2 — the same beans, different settings.
 *
 * <p>{@link com.example.ailab.week1.day2.NotificationProperties} binds {@code notify.*}.
 * {@code @Profile("dev")} and {@code @Profile("prod")} each register one
 * {@link com.example.ailab.week1.day2.Notifier}. {@code NotificationDraft} is
 * prototype-scoped; {@code NotificationService} is the default singleton and
 * asks for a new draft per call via {@code ObjectProvider}.
 */
package com.example.ailab.week1.day2;
