package com.github.mono83.events.json;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.function.Function;

/**
 * Writes event as byte array.
 */
public class JacksonByteWriter extends JacksonConfigurer implements Function<Object, byte[]> {
    @Override
    public byte[] apply(final Object o) {
        try {
            return mapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
