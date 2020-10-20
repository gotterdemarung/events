package com.github.mono83.events.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Publisher implements Consumer<Object> {
    private final Channel channel;
    private final String exchange;
    private final Function<Object, Object> wrapper;
    private final Function<Object, byte[]> byteConverter;

    public Publisher(
            final Channel channel,
            final String exchange,
            final Function<Object, Object> wrapper,
            final Function<Object, byte[]> byteConverter
    ) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.exchange = Objects.requireNonNull(exchange, "exchange");
        this.wrapper = wrapper == null ? Function.identity() : wrapper;
        this.byteConverter = Objects.requireNonNull(byteConverter, "byteConverter");
    }

    @Override
    public void accept(final Object o) {
        if (o != null) {
            Object wrapped = wrapper.apply(o);
            if (wrapped != null) {
                // Converting to bytes
                byte[] bytes = byteConverter.apply(o);
                // Publishing
                try {
                    channel.basicPublish(exchange, exchange, MessageProperties.PERSISTENT_TEXT_PLAIN, bytes);
                } catch (IOException e) {
                    throw new PublishingFailedException(o, wrapped, e);
                }
            }
        }
    }

    public static class PublishingFailedException extends RuntimeException {
        private final Object event;
        private final Object wrapped;

        PublishingFailedException(final Object o, final Object w, final IOException cause) {
            super(cause);
            this.event = o;
            this.wrapped = w;
        }

        public Object getOriginalEven() {
            return event;
        }

        public Object getWrappedEvent() {
            return wrapped;
        }
    }
}
