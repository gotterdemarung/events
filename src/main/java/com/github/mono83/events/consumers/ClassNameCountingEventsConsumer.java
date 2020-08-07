package com.github.mono83.events.consumers;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Counts consumed events by class name.
 */
public class ClassNameCountingEventsConsumer implements Consumer<Object> {
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public void accept(final Object event) {
        if (event != null) {
            counters.computeIfAbsent(event.getClass().getName(), $ -> new AtomicLong()).incrementAndGet();
        }
    }

    /**
     * @return Current counters value
     */
    public Map<String, Long> getCounters() {
        TreeMap<String, Long> response = new TreeMap<>();
        for (Map.Entry<String, AtomicLong> pair : counters.entrySet()) {
            response.put(pair.getKey(), pair.getValue().longValue());
        }
        return response;
    }
}
