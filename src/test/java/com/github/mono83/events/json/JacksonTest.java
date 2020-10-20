package com.github.mono83.events.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mono83.events.decorators.DeadLetterDecorator;
import com.github.mono83.events.decorators.DeferredEventDecorator;
import com.github.mono83.events.decorators.FingerprintEventDecorator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Instant;
import java.util.stream.Stream;

class JacksonTest {
    public static Stream<Arguments> jsonArguments() {
        return Stream.of(
                Arguments.of(
                        "[\"com.github.mono83.events.json.JacksonTest$StringEvent\",{\"value\":\"Hello\"}]",
                        new StringEvent("Hello")
                ),
                Arguments.of(
                        "[\"com.github.mono83.events.decorators.DeadLetterDecorator\",{\"event\":[\"com.github.mono83.events.json.JacksonTest$StringEvent\",{\"value\":\"Foo\"}]}]",
                        DeadLetterDecorator.of(new StringEvent("Foo"))
                ),
                Arguments.of(
                        "[\"com.github.mono83.events.decorators.DeferredEventDecorator\",{\"event\":[\"com.github.mono83.events.json.JacksonTest$StringEvent\",{\"value\":\"Bar\"}],\"until\":1234567890}]",
                        new DeferredEventDecorator<>(new StringEvent("Bar"), Instant.ofEpochMilli(1234567890000L))
                ),
                Arguments.of(
                        "[\"com.github.mono83.events.decorators.FingerprintEventDecorator\",{\"event\":[\"com.github.mono83.events.json.JacksonTest$StringEvent\",{\"value\":\"Baz\"}],\"id\":\"foo-bar-baz\",\"ver\":42}]",
                        new FingerprintEventDecorator<>(new StringEvent("Baz"), "foo-bar-baz", 42)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("jsonArguments")
    public void testJacksonMappings(final String json, final Object object) {
        JacksonByteWriter writer = new JacksonByteWriter();
        JacksonByteReader reader = new JacksonByteReader();

        Assertions.assertEquals(
                json,
                new String(writer.apply(object))
        );

        Assertions.assertEquals(
                object,
                reader.apply(json.getBytes())
        );
    }

    private static class StringEvent {
        @JsonProperty
        private final String value;

        @JsonCreator
        private StringEvent(@JsonProperty("value") final String value) {
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StringEvent that = (StringEvent) o;
            return value.equals(that.value);
        }
    }
}