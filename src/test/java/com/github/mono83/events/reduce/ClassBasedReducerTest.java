package com.github.mono83.events.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ClassBasedReducerTest {
    @Test
    public void testReduce() {
        List<Object> list = new ArrayList<>();
        list.add("foo");
        list.add(new StringsContainer("foo"));
        list.add(new CountingEvent(2));
        list.add(new CountingEvent(3));
        list.add(new CountingEvent(1));
        list.add("bar");

        List<Object> reduced = ClassBasedReducer.reduce(list);
        Assertions.assertEquals(4, reduced.size());
    }

    private static class CountingEvent implements Reducible<CountingEvent> {
        private final int value;

        private CountingEvent(final int value) {
            this.value = value;
        }

        @Override
        public CountingEvent reduce(final Collection<CountingEvent> other) {
            int sum = value;
            for (CountingEvent event : other) {
                sum += value;
            }

            return new CountingEvent(sum);
        }
    }

    private static class StringsContainer implements Reducible<StringsContainer> {
        private final Set<String> strings;

        private StringsContainer(final String single) {
            this(Collections.singleton(single));
        }

        private StringsContainer(final Set<String> strings) {
            this.strings = strings;
        }

        @Override
        public StringsContainer reduce(final Collection<StringsContainer> other) {
            HashSet<String> values = new HashSet<>();
            for (StringsContainer str : other) {
                values.addAll(str.strings);
            }

            return new StringsContainer(values);
        }
    }
}