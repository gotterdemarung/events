package com.github.mono83.events.consumers;

import com.github.mono83.events.Event;
import com.github.mono83.events.EventsConsumer;

/**
 * Special implementation of events consumer, that does nothing.
 */
public class BlackholeConsumer implements EventsConsumer {
    public static final BlackholeConsumer INSTANCE = new BlackholeConsumer();

    public BlackholeConsumer() {
    }

    @Override
    public void consume(final Event... events) {
        // Do nothing
    }

    @Override
    public void accept(final Event event) {
        // Do nothing
    }
}
