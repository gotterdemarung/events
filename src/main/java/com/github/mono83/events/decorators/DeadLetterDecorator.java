package com.github.mono83.events.decorators;

import com.github.mono83.events.AbstractEventDecorator;
import com.github.mono83.events.Event;

/**
 * Event decorator for events, that weren't handled by any consumer.
 */
public class DeadLetterDecorator<T extends Event> extends AbstractEventDecorator<T> {
    /**
     * Constructs new dead letter for given event.
     *
     * @param event Source event.
     * @return Event wrapped into dead letter decorator.
     */
    public static <T extends Event> DeadLetterDecorator<?> of(final T event) {
        if (event instanceof DeadLetterDecorator) {
            return of(((DeadLetterDecorator<?>) event).getEvent());
        }

        return new DeadLetterDecorator<>(event);
    }

    private DeadLetterDecorator(final T event) {
        super(event);
    }
}
