package com.github.mono83.events.consumers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

class ClassNameCountingEventsConsumerTest {
    @Test
    public void testConsume() {
        ClassNameCountingEventsConsumer consumer = new ClassNameCountingEventsConsumer();

        consumer.accept(new Object());
        consumer.accept("foo");
        consumer.accept("bar");
        consumer.accept(10f);

        // Reading counters
        Map<String, Long> counters = consumer.getCounters();
        Assertions.assertTrue(counters instanceof TreeMap);
        Assertions.assertEquals(3, counters.size());
        Assertions.assertEquals(1, counters.get(Object.class.getName()));
        Assertions.assertEquals(2, counters.get(String.class.getName()));
        Assertions.assertEquals(1, counters.get(Float.class.getName()));
    }
}