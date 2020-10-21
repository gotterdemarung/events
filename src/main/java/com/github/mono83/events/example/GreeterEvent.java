package com.github.mono83.events.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class GreeterEvent {
    private final String name;

    @JsonCreator
    public GreeterEvent(@JsonProperty("n") final String name) {
        this.name = Objects.requireNonNull(name);
    }

    @JsonProperty("n")
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Hello, " + getName();
    }
}
