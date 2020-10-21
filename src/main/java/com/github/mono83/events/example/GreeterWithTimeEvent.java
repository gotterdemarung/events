package com.github.mono83.events.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class GreeterWithTimeEvent extends GreeterEvent {
    private final Instant time;

    @JsonCreator
    public GreeterWithTimeEvent(@JsonProperty("n") final String name, @JsonProperty("u") final long unix) {
        super(name);
        this.time = Instant.ofEpochSecond(unix);
    }

    public GreeterWithTimeEvent(final String name) {
        super(name);
        this.time = Instant.now();
    }

    @JsonIgnore
    public Instant getTime() {
        return time;
    }

    @JsonProperty("u")
    public long getTimeUnix() {
        return getTime().getEpochSecond();
    }

    @Override
    public String toString() {
        return super.toString() + ". It is " + getTime();
    }
}
