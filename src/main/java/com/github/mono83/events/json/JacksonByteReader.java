package com.github.mono83.events.json;

import java.io.IOException;
import java.util.function.Function;

/**
 * Reads data from byte array into event.
 */
public class JacksonByteReader extends JacksonConfigurer implements Function<byte[], Object> {
    @Override
    public Object apply(final byte[] bytes) {
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
