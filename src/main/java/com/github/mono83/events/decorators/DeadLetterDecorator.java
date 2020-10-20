package com.github.mono83.events.decorators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mono83.events.AbstractEventDecorator;

/**
 * Event decorator for events, that weren't handled by any consumer.
 */
public class DeadLetterDecorator<T> extends AbstractEventDecorator<T> {
    /**
     * Constructs new dead letter for given event.
     *
     * @param event Source event.
     * @return Event wrapped into dead letter decorator.
     */
    @SuppressWarnings("unchecked")
    @JsonCreator
    public static <T> DeadLetterDecorator<T> of(@JsonProperty("event") final T event) {
        if (event instanceof DeadLetterDecorator) {
            return of(((DeadLetterDecorator<T>) event).getEvent());
        }

        return new DeadLetterDecorator<>(event);
    }

    /**
     * Private constructor.
     *
     * @param event Event to decorate.
     */
    private DeadLetterDecorator(final T event) {
        super(event);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeadLetterDecorator<?> that = (DeadLetterDecorator<?>) o;
        return this.getEvent().equals(that.getEvent());
    }
}
