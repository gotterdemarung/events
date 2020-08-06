package com.github.mono83.events.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mono83.events.Event;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

class RabbitMqConsumer extends DefaultConsumer {
    private final Consumer<Event> consumer;
    private final ObjectMapper mapper;

    public RabbitMqConsumer(
            final Channel channel,
            final ObjectMapper deserializer,
            final Consumer<Event> consumer
    ) {
        super(channel);
        this.mapper = Objects.requireNonNull(deserializer, "deserializer");
        this.consumer = Objects.requireNonNull(consumer, "consumer");
    }

    @Override
    public void handleDelivery(
            final String consumerTag,
            final Envelope envelope,
            final AMQP.BasicProperties properties,
            final byte[] body
    ) throws IOException {
        // Deserialize
        consumer.accept(mapper.readValue(body, Event.class));
    }
}
