package com.github.mono83.events.decorators;

import com.github.mono83.events.AbstractEventDecorator;

import java.time.Instant;
import java.util.Objects;

/**
 * Contains other event, that should be invoked no sooner that configured time.
 */
public class DeferredEventDecorator<T> extends AbstractEventDecorator<T> {
    private final Instant until;

    /**
     * Constructs new deferred event.
     *
     * @param event Event to defer.
     * @param until Event schedule time.
     */
    public DeferredEventDecorator(final T event, final Instant until) {
        super(event);
        this.until = Objects.requireNonNull(until, "until");
    }

    /**
     * @return Time instant this event scheduled for.
     */
    public final Instant getUntil() {
        return until;
    }

    @Override
    public String toString() {
        return String.format(
                "{%s@%s}",
                getEvent().toString(),
                getUntil().toString()
        );
    }
}