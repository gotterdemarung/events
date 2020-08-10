package com.github.mono83.events.consumers;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Events consumer, that writes data into given target collection.
 */
class CollectionConsumer implements Consumer<Object> {
    private final Collection<Object> target;

    public CollectionConsumer(final Collection<Object> target) {
        this.target = Objects.requireNonNull(target, "target");
    }

    @Override
    public void accept(final Object event) {
        if (event != null) {
            target.add(event);
        }
    }
}
