package com.github.mono83.events.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.mono83.events.decorators.DeadLetterDecorator;

import java.io.IOException;

public class DeadLetterDecoratorSerializer extends JsonSerializer<DeadLetterDecorator> {
    @Override
    public void serialize(
            final DeadLetterDecorator value,
            final JsonGenerator gen,
            final SerializerProvider serializers
    ) throws IOException {
        gen.writeObject(value.getEvent());
    }
}
