package com.github.mono83.events;

import java.util.function.Consumer;

public interface ClassNameHandlerRegistry {
    /**
     * Registers given events consumer.
     *
     * @param consumer Events consumer.
     * @param classes  Event classes to listen. If no classes given, will listen for all event types.
     */
    void register(
            final Consumer<?> consumer,
            final Class<?>... classes
    );
}
