package com.github.mono83.events.reduce;

import java.util.Collection;
import java.util.List;

/**
 * Defines events, that can be reduced (merged)
 */
public interface Reducible<T extends Reducible<T>> {
    /**
     * Reduces (merges) items within collection.
     *
     * @param events Events to merge.
     * @return Single event.
     */
    static <T extends Reducible<T>> T reduceCollection(final List<T> events) {
        if (events == null || events.size() == 0) {
            throw new IllegalArgumentException("Empty reducible events collection");
        } else if (events.size() == 1) {
            return events.iterator().next();
        }

        return events.get(0).reduce(events.subList(1, events.size()));
    }

    /**
     * Merges current event with other of same type.
     *
     * @param other Other event to merge with.
     * @return Merged event.
     */
    T reduce(Collection<T> other);
}
