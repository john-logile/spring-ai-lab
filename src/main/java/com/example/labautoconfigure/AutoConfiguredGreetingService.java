package com.example.labautoconfigure;

/** Fallback greeting used only when the application does not define its own. */
public class AutoConfiguredGreetingService implements GreetingService {

    @Override
    public String greet() {
        return "Hello from auto-configuration";
    }

    @Override
    public String origin() {
        return "auto-config";
    }
}
