package com.example.labautoconfigure;

/**
 * Owned by the auto-configuration, not by the application.
 * The app may replace the bean; callers still inject this type.
 */
public interface GreetingService {

    String greet();

    /** {@code auto-config} or {@code user bean}. */
    String origin();
}
