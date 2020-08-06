package com.github.mono83.events.concurrent;

import com.github.mono83.events.Event;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Decorator to standard runnable.
 */
public class EventPublisher implements Runnable {
    private final Consumer<Event> consumer;
    private final Event event;

    /**
     * Finds all events publishers and makes invokes run method on found.
     *
     * @param candidates Runnables
     */
    public static void findAndRun(final Iterable<Runnable> candidates) {
        for (Runnable candidate : candidates) {
            if (candidate instanceof EventPublisher) {
                candidate.run();
            }
        }
    }

    public EventPublisher(final Consumer<Event> consumer, final Event event) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.event = Objects.requireNonNull(event, "event");
    }

    @Override
    public void run() {
        consumer.accept(event);
    }

    /**
     * @return Event to publish
     */
    public Event getEvent() {
        return this.event;
    }
}
