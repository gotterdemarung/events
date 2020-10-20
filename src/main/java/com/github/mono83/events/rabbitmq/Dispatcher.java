package com.github.mono83.events.rabbitmq;

import com.github.mono83.events.decorators.DeadLetterDecorator;
import com.github.mono83.events.decorators.DeferredEventDecorator;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class Dispatcher implements Consumer<Object> {
    private final TreeMap<Duration, Publisher> publishers = new TreeMap<>(Collections.reverseOrder());
    private final Publisher realtime;
    private final Publisher deadLetter;
    private final Consumer<Object> router;

    public static void initialize(
            final Channel channel,
            final String exchange,
            final Collection<Duration> thresholds
    ) throws IOException {
        Objects.requireNonNull(channel, "channel");
        Objects.requireNonNull(exchange, "exchange");
        Objects.requireNonNull(thresholds, "thresholds");

        // Declaring main exchange
        AMQP.Exchange.DeclareOk main = channel.exchangeDeclare(exchange, BuiltinExchangeType.FANOUT);

        // Declaring dead-letter queue and exchange
        channel.exchangeDeclare(exchange + "_dl", BuiltinExchangeType.DIRECT);
        channel.queueDeclare(exchange + "_dl", true, false, false, null);
        channel.queueBind(exchange + "_dl", exchange + "_dl", exchange + "_dl");

        // Declaring delayed TTL queues
        for (Duration th : thresholds) {
            String name = exchange + "_" + th.toSeconds();
            HashMap<String, Object> args = new HashMap<>();
            args.put("x-dead-letter-exchange", exchange);
            args.put("x-message-ttl", th.toMillis());
            channel.exchangeDeclare(name, BuiltinExchangeType.DIRECT);
            channel.queueDeclare(name, true, false, false, args);
            channel.queueBind(name, name, name);
        }
    }

    public Dispatcher(
            final Connection connection,
            final String exchange,
            final Collection<Duration> thresholds,
            final Function<byte[], Object> fromByteConverter,
            final Function<Object, byte[]> toByteConverter,
            final Consumer<Object> router
    ) throws IOException {
        Objects.requireNonNull(connection, "connection");
        Objects.requireNonNull(exchange, "exchange");
        Objects.requireNonNull(thresholds, "thresholds");
        Objects.requireNonNull(fromByteConverter, "fromByteConverter");
        Objects.requireNonNull(toByteConverter, "toByteConverter");
        Objects.requireNonNull(router, "router");

        try (Channel channel = connection.createChannel()) {
            initialize(channel, exchange, thresholds);
        } catch (TimeoutException e) {
            throw new IOException(e);
        }

        this.router = router;

        // Initializing wrappers
        this.realtime = new Publisher(connection.createChannel(), exchange, null, toByteConverter);
        this.deadLetter = new Publisher(connection.createChannel(), exchange + "_dl", null, toByteConverter);
        for (Duration threshold : thresholds) {
            publishers.put(
                    threshold,
                    new Publisher(connection.createChannel(), exchange + "_" + threshold.toSeconds(), null, toByteConverter)
            );
        }
        new Reader(connection.createChannel(), exchange, fromByteConverter, this::incoming);
    }

    @Override
    public final void accept(final Object o) {
        this.outgoing(o);
    }

    private void outgoing(final Object raw) {
        if (raw == null) {
            return;
        }
        Object event = onBeforeOutgoing(raw);

        if (event instanceof DeferredEventDecorator<?>) {
            DeferredEventDecorator<?> deferred = (DeferredEventDecorator<?>) event;
            Instant now = Instant.now();
            Instant until = deferred.getUntil();
            if (!until.isAfter(now)) {
                // Event already occurred
                realtime.accept(deferred.getEvent());
            } else {
                Duration delta = Duration.between(now, until);
                Publisher chosen = null;
                for (Map.Entry<Duration, Publisher> entry : publishers.entrySet()) {
                    chosen = entry.getValue();
                    if (delta.compareTo(entry.getKey()) >= 0) {
                        break;
                    }
                }
                chosen.accept(deferred);
            }
        } else if (event instanceof DeadLetterDecorator<?>) {
            DeadLetterDecorator<?> deadLetterDecorator = (DeadLetterDecorator<?>) event;
            deadLetter.accept(deadLetterDecorator);
        } else if (event != null) {
            realtime.accept(event);
        }
    }

    private void incoming(final Object raw) {
        if (raw == null) {
            return;
        }
        Object event = onBeforeIncoming(raw);

        if (event instanceof DeferredEventDecorator<?> || event instanceof DeadLetterDecorator<?>) {
            this.outgoing(event);
        } else if (event != null) {
            router.accept(event);
        }
    }

    protected Object onBeforeOutgoing(final Object o) {
        return o;
    }

    protected Object onBeforeIncoming(final Object o) {
        return o;
    }
}
