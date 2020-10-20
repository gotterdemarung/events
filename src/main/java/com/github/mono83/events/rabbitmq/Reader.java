package com.github.mono83.events.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Reader extends DefaultConsumer {
    private final Function<byte[], Object> byteConverter;
    private final Consumer<Object> router;

    public Reader(
            final Channel channel,
            final String exchange,
            final Function<byte[], Object> byteConverter,
            final Consumer<Object> router
    ) throws IOException {
        super(Objects.requireNonNull(channel, "channel"));
        Objects.requireNonNull(exchange, "exchange");
        this.byteConverter = Objects.requireNonNull(byteConverter, "byteConverter");
        this.router = Objects.requireNonNull(router, "router");

        // Creating destination queue and binding it to exchange
        AMQP.Queue.DeclareOk declare = getChannel().queueDeclare();
        getChannel().queueBind(declare.getQueue(), exchange, "");
        getChannel().basicConsume(declare.getQueue(), true, this);
    }

    @Override
    public void handleDelivery(
            final String consumerTag,
            final Envelope envelope,
            final AMQP.BasicProperties properties,
            final byte[] body
    ) throws IOException {
        try {
            // Converting bytes to event
            Object event = byteConverter.apply(body);
            // Sending event to router
            router.accept(event);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
