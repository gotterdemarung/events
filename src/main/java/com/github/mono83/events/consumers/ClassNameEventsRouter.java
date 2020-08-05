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
                this.consume(event);
            }
        }
    }

    public void register(
            final EventsConsumer consumer,
            final Class<?>... classes
    ) {
        if (consumer != null && classes != null && classes.length > 0) {
            for (Class<?> clazz : classes) {
                if (clazz == Event.class) {
                    rootConsumers.add(consumer);
                } else {
                    consumers.computeIfAbsent(clazz, ($) -> new CopyOnWriteArrayList<>()).add(consumer);
                }
            }
        }
    }

    private void consume(final Event event) {
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
