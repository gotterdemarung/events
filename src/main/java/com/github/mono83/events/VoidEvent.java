package com.github.mono83.events;

/**
 * Void implementation of event.
 */
public class VoidEvent implements Event {
    public static final VoidEvent INSTANCE = new VoidEvent();

    private VoidEvent() {
    }

    @Override
    public String toString() {
        return "{Void}";
    }
}
