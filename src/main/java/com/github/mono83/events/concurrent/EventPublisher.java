package com.github.mono83.events.concurrent;

import com.github.mono83.events.AbstractEventDecorator;
import com.github.mono83.events.Event;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Decorator to standard runnable.
 */
public class EventPublisher implements Runnable {
    private final Consumer<Event> consumer;
    private final Event event;
    private final boolean unfold;

    /**
     * Finds all events publishers and makes invokes drains raw event from them.
     *
     * @param candidates Runnables
     */
    public static void findAndDrain(final Iterable<Runnable> candidates) {
        for (Runnable candidate : candidates) {
            if (candidate instanceof EventPublisher) {
                EventPublisher ep = (EventPublisher) candidate;
                ep.consumer.accept(ep.event);
            }
        }
    }

    /**
     * Constructs runnable event publisher, that will send event to consumer on "run" method invocation
     *
     * @param consumer Consumer to send data into
     * @param event    Event to send
     */
    public EventPublisher(final Consumer<Event> consumer, final Event event) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.event = Objects.requireNonNull(event, "event");
        this.unfold = false;
    }

    /**
     * Constructs runnable event publisher, that will send event to consumer on "run" method invocation
     *
     * @param consumer Consumer to send data into
     * @param event    Event to send
     * @param unfold   If true, publisher will send inner event inside decorated one.
     */
    public EventPublisher(final Consumer<Event> consumer, final AbstractEventDecorator<?> event, final boolean unfold) {
        this.consumer = Objects.requireNonNull(consumer, "consumer");
        this.event = Objects.requireNonNull(event, "event");
        this.unfold = unfold;
    }

    @Override
    public void run() {
        if (unfold) {
            consumer.accept(((AbstractEventDecorator<?>) event).getEvent());
        } else {
            consumer.accept(event);
        }
    }

    /**
     * @return Event to publish
     */
    public Event getEvent() {
        return this.event;
    }
}
