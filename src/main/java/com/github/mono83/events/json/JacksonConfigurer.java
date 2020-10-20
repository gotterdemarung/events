package com.github.mono83.events.json;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Base abstract class for Jackson JSON
 * Configures object mapper.
 */
abstract class JacksonConfigurer {
    protected final ObjectMapper mapper;

    protected JacksonConfigurer() {
        this.mapper = new ObjectMapper();
        this.mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.EVERYTHING);
    }
}
