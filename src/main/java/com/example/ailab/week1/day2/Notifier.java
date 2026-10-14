package com.example.ailab.week1.day2;

/**
 * What the rest of the app depends on. Dev and prod each supply one bean of this type.
 */
public interface Notifier {

    String deliver(String message);
}
