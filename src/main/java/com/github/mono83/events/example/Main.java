package com.github.mono83.events.example;

import com.github.mono83.events.decorators.DeferredEventDecorator;
import com.github.mono83.events.rabbitmq.Dispatcher;
import com.github.mono83.events.spring.EventHandlerAnnotationBeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.Duration;
import java.time.Instant;

/**
 * This example requires some dependencies, that are marked "provided"
 * To make it work - comment <scope>provided</scope> in main pom.xml file
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                MainConfiguration.class,
                EventHandlerAnnotationBeanPostProcessor.class,
                Handlers.class
        );

        System.out.println(Instant.now() + " Sending plain events");
        context.getBean(Dispatcher.class).accept(new GreeterEvent("World"));
        context.getBean(Dispatcher.class).accept(new GreeterWithTimeEvent("World"));
        Thread.sleep(1000);

        System.out.println(Instant.now() + " Deferring for 3s");
        context.getBean(Dispatcher.class).accept(new DeferredEventDecorator<>(new GreeterEvent("World"), Duration.ofSeconds(3)));
    }
}
