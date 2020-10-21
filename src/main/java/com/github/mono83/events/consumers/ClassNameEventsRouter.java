package com.github.mono83.events.consumers;


import com.github.mono83.events.ClassNameHandlerRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Events consumer, that routes events by class name.
 */
public class ClassNameEventsRouter implements ClassNameHandlerRegistry, Consumer<Object> {
    private final ConcurrentHashMap<Class<?>, Class<?>[]> inheritanceTree = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Class<?>, List<Consumer<Object>>> consumers = new ConcurrentHashMap<>();
    private final List<Consumer<Object>> rootConsumers = new CopyOnWriteArrayList<>();

    @Override
    public void accept(final Object event) {
        if (event != null) {
            dispatch(event);
        }
    }

    /**
     * Registers given events consumer.
     *
     * @param consumer Events consumer.
     * @param classes  Event classes to listen. If no classes given, will listen for all event types.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void register(
            final Consumer<?> consumer,
            final Class<?>... classes
    ) {
        if (consumer == null) {
            // No consumer given
            return;
        }

        if (classes != null && classes.length > 0) {
            for (Class<?> clazz : classes) {
                if (clazz == Object.class) {
                    rootConsumers.add((Consumer<Object>) consumer);
                } else {
                    consumers.computeIfAbsent(clazz, ($) -> new CopyOnWriteArrayList<>()).add((Consumer<Object>) consumer);
                }
            }
        } else {
            // Registering on root level
            rootConsumers.add((Consumer<Object>) consumer);
        }
    }

    /**
     * Sends given event to registered consumers.
     *
     * @param event Event to dispatch.
     */
    private void dispatch(final Object event) {
        Class<?> clazz = event.getClass();
        for (Class<?> c : inheritanceTree.computeIfAbsent(clazz, ClassNameEventsRouter::buildInheritanceTreeForClass)) {
            List<Consumer<Object>> eventsConsumers = consumers.get(c);
            if (eventsConsumers != null) {
                for (Consumer<Object> consumer : eventsConsumers) {
                    consumer.accept(event);
                }
            }
        }

        for (Consumer<Object> consumer : rootConsumers) {
            consumer.accept(event);
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
        while (sup != Object.class) {
            classes.add(sup);
            sup = sup.getSuperclass();
        }
        return classes.toArray(new Class<?>[0]);
    }
}
