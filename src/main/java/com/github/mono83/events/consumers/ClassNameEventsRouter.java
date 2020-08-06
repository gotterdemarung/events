package com.github.mono83.events.consumers;


import com.github.mono83.events.Event;
import com.github.mono83.events.EventsConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Events consumer, that routes events by class name.
 */
public class ClassNameEventsRouter implements EventsConsumer {
    private final ConcurrentHashMap<Class<?>, Class<?>[]> inheritanceTree = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, List<EventsConsumer>> consumers = new ConcurrentHashMap<>();
    private final List<EventsConsumer> rootConsumers = new CopyOnWriteArrayList<>();

    @Override
    public void consume(final Event... events) {
        if (events != null && events.length > 0) {
            for (Event event : events) {
                this.dispatch(event);
            }
        }
    }

    /**
     * Registers given events consumer.
     *
     * @param consumer Events consumer.
     * @param classes  Event classes to listen. If no classes given, will listen for all event types.
     */
    public void register(
            final EventsConsumer consumer,
            final Class<?>... classes
    ) {
        if (consumer == null) {
            // No consumer given
            return;
        }

        if (classes != null && classes.length > 0) {
            for (Class<?> clazz : classes) {
                if (clazz == Event.class) {
                    rootConsumers.add(consumer);
                } else {
                    consumers.computeIfAbsent(clazz, ($) -> new CopyOnWriteArrayList<>()).add(consumer);
                }
            }
        } else {
            // Registering on root level
            rootConsumers.add(consumer);
        }
    }

    /**
     * Sends given event to registered consumers.
     *
     * @param event Event to dispatch.
     */
    private void dispatch(final Event event) {
        Class<?> clazz = event.getClass();
        for (Class<?> c : inheritanceTree.computeIfAbsent(clazz, ClassNameEventsRouter::buildInheritanceTreeForClass)) {
            List<EventsConsumer> eventsConsumers = consumers.get(c);
            if (eventsConsumers != null) {
                for (EventsConsumer consumer : eventsConsumers) {
                    consumer.consume(event);
                }
            }
        }

        for (EventsConsumer consumer : rootConsumers) {
            consumer.consume(event);
        }
    }

    /**
     * Static helper method, that generates classes from inheritance tree.
     *
     * @param clazz Class for lookup
     * @return Class inheritance up to {@see Event}
     */
    private static Class<?>[] buildInheritanceTreeForClass(final Class<?> clazz) {
        ArrayList<Class<?>> classes = new ArrayList<>();
        classes.add(clazz);
        Class<?> sup = clazz.getSuperclass();
        while (Event.class.isAssignableFrom(sup)) {
            classes.add(sup);
            sup = sup.getSuperclass();
        }
        return classes.toArray(new Class<?>[0]);
    }
}
