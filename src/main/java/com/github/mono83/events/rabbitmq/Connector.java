package com.github.mono83.events.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mono83.events.Event;
import com.github.mono83.events.EventsConsumer;
import com.github.mono83.events.decorators.DeadLetterDecorator;
import com.github.mono83.events.decorators.DeferredEventDecorator;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class Connector implements EventsConsumer, Closeable, ShutdownListener {
    private final ConnectionFactory connectionFactory;
    private final String exchangeName;
    private final String mainQueueName;
    private final String shortDelayQueueName;
    private final String longDelayQueueName;
    private final Duration shortDelay;
    private final Duration longDelay;
    private final ObjectMapper mapper;
    volatile Connection connection;
    volatile Channel channel;

    public Connector(
            final ConnectionFactory connectionFactory,
            final String exchangeName,
            final String mainQueueName,
            final String shortDelayQueueName,
            final String longDelayQueueName,
            final Duration shortDelay,
            final Duration longDelay
    ) {
        this.connectionFactory = Objects.requireNonNull(connectionFactory, "connectionFactory");
        this.exchangeName = Objects.requireNonNull(exchangeName, "exchangeName");
        this.mainQueueName = Objects.requireNonNull(mainQueueName, "mainQueueName");
        this.shortDelayQueueName = Objects.requireNonNull(shortDelayQueueName, "shortDelayQueueName");
        this.longDelayQueueName = Objects.requireNonNull(longDelayQueueName, "longDelayQueueName");
        this.shortDelay = Objects.requireNonNull(shortDelay, "shortDelay");
        this.longDelay = Objects.requireNonNull(longDelay, "longDelay");

        this.mapper = new ObjectMapper();
    }


    @Override
    public void consume(final Event... events) {
        if (events != null && events.length > 0) {
            for (Event event : events) {
                this.publish(event);
            }
        }
    }

    private void publish(final Event event) {
        if (event instanceof DeadLetterDecorator) {
            this.publish(((DeadLetterDecorator<?>) event).getEvent());
            return;
        }

        String exchange, routingKey;
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        if (event instanceof DeferredEventDecorator) {
            DeferredEventDecorator<?> d = (DeferredEventDecorator<?>) event;
            Instant now = Instant.now();
            if (!d.getUntil().isAfter(now)) {
                // Time has come
                exchange = exchangeName;
                routingKey = "";
            } else {
                exchange = "";
                routingKey = Duration.between(now, d.getUntil()).compareTo(longDelay) >= 0
                        ? longDelayQueueName
                        : shortDelayQueueName;
            }
        } else {
            exchange = exchangeName;
            routingKey = "";
        }

        try {
            channel.basicPublish(exchange, routingKey, builder.build(), mapper.writeValueAsBytes(event));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void subscribeToQueue(final String queueName, final Consumer<Event> consumer) {
        try {
            Channel channel = connection.createChannel();
            channel.basicConsume(queueName, true, new RabbitMqConsumer(channel, mapper, consumer));
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    public void subscribeToExchange(final String exchangeName, final Consumer<Event> consumer) {
        try {
            Channel channel = connection.createChannel();
            AMQP.Queue.DeclareOk specs = channel.queueDeclare();
            channel.queueBind(specs.getQueue(), exchangeName, "");
            channel.basicConsume(specs.getQueue(), true, new RabbitMqConsumer(channel, mapper, consumer));
        } catch (IOException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    public synchronized void open() throws IOException, IllegalStateException, TimeoutException {
        if (connection != null) {
            throw new IllegalStateException("Connection already established");
        }

        // Establishing connection
        connection = connectionFactory.newConnection();
        connection.addShutdownListener(this);
    }

    public synchronized void constructExchange() throws IOException, TimeoutException {
        try (Channel channel = connection.createChannel()) {
            // Declaring exchange
            channel.exchangeDeclare(exchangeName, "fanout");

            // Declaring main queue
            channel.queueDeclare(mainQueueName, true, false, false, null);
            channel.queueBind(mainQueueName, exchangeName, "");

            // Declaring short delay queue
            channel.queueDeclare(
                    shortDelayQueueName,
                    true,
                    false,
                    false,
                    Map.of(
                            "x-dead-letter-exchange", exchangeName,
                            "x-message-ttl", shortDelay.toMillis()
                    )
            );

            // Declaring long delay queue
            channel.queueDeclare(
                    longDelayQueueName,
                    true,
                    false,
                    false,
                    Map.of(
                            "x-dead-letter-exchange", exchangeName,
                            "x-message-ttl", longDelay.toMillis()
                    )
            );
        }
    }

    @Override
    public void shutdownCompleted(final ShutdownSignalException e) {
        if (connection != null) {
            connection = null;
        }
    }
}
