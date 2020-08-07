package com.github.mono83.events.consumers;

import java.util.function.Consumer;

/**
 * Special implementation of events consumer, that does nothing.
 */
public class BlackholeConsumer implements Consumer<Object> {
    public static final BlackholeConsumer INSTANCE = new BlackholeConsumer();

    public BlackholeConsumer() {
    }

    @Override
    public void accept(final Object event) {
        // Do nothing
    }
}
