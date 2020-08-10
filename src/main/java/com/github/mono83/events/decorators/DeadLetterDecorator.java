package com.github.mono83.events.decorators;

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
    public static <T> DeadLetterDecorator<T> of(final T event) {
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
}
