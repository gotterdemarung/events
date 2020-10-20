package com.github.mono83.events.decorators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mono83.events.AbstractEventDecorator;

import java.time.Duration;
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
     * Constructs new deferred event.
     *
     * @param event            Event to defer.
     * @param untilEpochSecond Event schedule time.
     */
    @JsonCreator
    public DeferredEventDecorator(
            @JsonProperty("event") final T event,
            @JsonProperty("until") final long untilEpochSecond
    ) {
        this(event, Instant.ofEpochMilli(1000 * untilEpochSecond));
    }

    /**
     * Constructs new deferred event using given delay value.
     *
     * @param event Event to defer.
     * @param delay Event delay.
     */
    public DeferredEventDecorator(final T event, final Duration delay) {
        this(
                event,
                Instant.now().plus(delay)
        );
    }

    /**
     * @return Time instant this event scheduled for.
     */
    @JsonIgnore
    public final Instant getUntil() {
        return until;
    }

    /**
     * @return Unix timestamp with seconds precision this event scheduled for.
     */
    @JsonProperty("until")
    public long getUntilUnix() {
        return getUntil().getEpochSecond();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeferredEventDecorator<?> that = (DeferredEventDecorator<?>) o;
        return Objects.equals(this.until, that.until) && this.getEvent().equals(that.getEvent());
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