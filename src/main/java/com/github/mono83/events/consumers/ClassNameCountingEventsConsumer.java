package com.github.mono83.events.consumers;

import com.github.mono83.events.Event;
import com.github.mono83.events.EventsConsumer;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Counts consumed events by class name.
 */
public class ClassNameCountingEventsConsumer implements EventsConsumer {
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public void consume(final Event... events) {
        if (events != null && events.length > 0) {
            for (Event event : events) {
                counters.computeIfAbsent(event.getClass().getName(), $ -> new AtomicLong()).incrementAndGet();
            }
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
