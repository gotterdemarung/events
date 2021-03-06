package com.github.mono83.events;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Abstract wrapper to be used in event decorators.
 */
public abstract class AbstractEventDecorator<T> {
    private final T event;

    /**
     * Constructs new decorator instance.
     *
     * @param event Event to decorate.
     */
    protected AbstractEventDecorator(final T event) {
        this.event = Objects.requireNonNull(event, "event");
    }

    /**
     * @return Original event.
     */
    @JsonProperty("event")
    public T getEvent() {
        return event;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEvent());
    }
}
