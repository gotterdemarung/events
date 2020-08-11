package com.github.mono83.events.reduce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ReducibleTest {
    @Test
    public void testReduce() {
        List<StringsContainer> list = new ArrayList<>();
        list.add(new StringsContainer("foo"));
        list.add(new StringsContainer("bar"));
        list.add(new StringsContainer("baz"));
        list.add(new StringsContainer("foo"));

        StringsContainer fin = Reducible.reduceCollection(list);
        Assertions.assertEquals(3, fin.strings.size());
        Assertions.assertTrue(fin.strings.contains("foo"));
        Assertions.assertTrue(fin.strings.contains("bar"));
        Assertions.assertTrue(fin.strings.contains("baz"));
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