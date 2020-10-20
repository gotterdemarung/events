package com.github.mono83.events.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mono83.events.consumers.ClassNameEventsRouter;
import com.github.mono83.events.consumers.DebugEventNamePrinter;
import com.github.mono83.events.decorators.DeferredEventDecorator;
import com.github.mono83.events.json.JacksonByteReader;
import com.github.mono83.events.json.JacksonByteWriter;
import com.github.mono83.events.rabbitmq.Dispatcher;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("foo");
        factory.setUsername("guest");
        factory.setPassword("guest");

        ClassNameEventsRouter router = new ClassNameEventsRouter();
        router.register(new DebugEventNamePrinter(), Object.class);
//        router.register(new DebugEventNamePrinter(), CounterEvent.class);

        Dispatcher dispatcher = new Dispatcher(
                factory.newConnection(),
                "eventbus",
                List.of(Duration.ofSeconds(5), Duration.ofMinutes(1), Duration.ofHours(1)),
                new JacksonByteReader(),
                new JacksonByteWriter(),
                router
        ) {
            @Override
            protected Object onBeforeIncoming(final Object o) {
                System.out.println("INCOMING");
                return o;
            }
        };

        Random random = new Random();

        while (true) {
            Thread.sleep(500);
            Duration delay = Duration.ofMillis(random.nextInt(3000));
            System.out.println("Sending with delay " + delay);
//            dispatcher.accept(new CounterEvent());
            dispatcher.accept(new DeferredEventDecorator<>(new CounterEvent(), delay));
        }
    }

    public static class CounterEvent {
        private static final AtomicLong counter = new AtomicLong();
        @JsonProperty
        private long i;

        public CounterEvent(@JsonProperty("i") long i) {
            this.i = i;
        }

        public CounterEvent() {
            this.i = counter.incrementAndGet();
        }

        @Override
        public String toString() {
            return "I'm #" + i;
        }
    }
}
