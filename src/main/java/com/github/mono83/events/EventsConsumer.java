package com.github.mono83.events;

import java.util.function.Consumer;

/**
 * Defines events consumer - components, responsible for event handling.
 * By design, consumers accepts multiple events at once.
 */
@FunctionalInterface
public interface EventsConsumer extends Consumer<Event> {
    /**
     * Consumes given events.
     * If no events given, consumer by contract should do nothing.
     *
     * @param events Events to consume.
     */
    void consume(Event... events);

    @Override
    default void accept(Event event) {
        this.consume(event);
    }
}