package com.github.mono83.events.example;

import com.github.mono83.events.consumers.ClassNameEventsRouter;
import com.github.mono83.events.json.JacksonByteReader;
import com.github.mono83.events.json.JacksonByteWriter;
import com.github.mono83.events.rabbitmq.Dispatcher;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Configuration
public class MainConfiguration {
    @Bean
    public ConnectionFactory getConnectionFactory() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("foo");
        factory.setUsername("guest");
        factory.setPassword("guest");
        return factory;
    }

    @Bean
    public ClassNameEventsRouter getRouter() {
        return new ClassNameEventsRouter();
    }

    @Bean
    public Dispatcher getRabbitMqDispatcher(
            final ConnectionFactory factory,
            final ClassNameEventsRouter router
    ) throws IOException, TimeoutException {
        return new Dispatcher(
                factory.newConnection(),
                "eventbus",
                List.of(Duration.ofSeconds(1), Duration.ofMinutes(1), Duration.ofHours(1)),
                new JacksonByteReader(),
                new JacksonByteWriter(),
                router
        );
    }
}
