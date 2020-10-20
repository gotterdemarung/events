package com.github.mono83.events.decorators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.mono83.events.AbstractEventDecorator;

import java.util.Objects;

/**
 * Adds versioning to events.
 */
public class FingerprintEventDecorator<T> extends AbstractEventDecorator<T> {
    private final int version;
    private final String tracingId;

    /**
     * Constructs new decorator instance.
     *
     * @param event     Event to decorate.
     * @param tracingId Tracing identifier.
     * @param version   Version.
     */
    @JsonCreator
    public FingerprintEventDecorator(
            @JsonProperty("event") final T event,
            @JsonProperty("id") final String tracingId,
            @JsonProperty("ver") final int version
    ) {
        super(event);
        this.tracingId = Objects.requireNonNull(tracingId, "tracingId");
        this.version = version;
    }

    /**
     * @return Version
     */
    @JsonProperty("ver")
    public int getVersion() {
        return version;
    }

    /**
     * @return Tracing identifier
     */
    @JsonProperty("id")
    public String getTracingId() {
        return tracingId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FingerprintEventDecorator<?> that = (FingerprintEventDecorator<?>) o;
        return this.version == that.version
                && Objects.equals(this.tracingId, that.tracingId)
                && this.getEvent().equals(that.getEvent());
    }

    @Override
    public String toString() {
        return String.format(
                "{%s v%d}",
                getEvent().toString(),
                getVersion()
        );
    }
}
